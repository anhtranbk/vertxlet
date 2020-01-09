package com.admicro.vertxlet.core;

import com.admicro.vertxlet.core.db.DatabaseHandler;
import com.admicro.vertxlet.core.db.Jdbc;
import com.admicro.vertxlet.core.db.Redis;
import com.admicro.vertxlet.core.handler.FailureHandler;
import com.admicro.vertxlet.core.handler.InitializeHandler;
import com.admicro.vertxlet.core.mvc.RequestDispatcher;
import com.admicro.vertxlet.util.SimpleClassLoader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerVerticle extends AbstractVerticle {

    public static final String DATABASE_KEY = "db";
    public static final String DEFAULT_SHARE_LOCAL_MAP = ServerVerticle.class.getName();

    private static final Logger logger = LoggerFactory.getLogger(ServerVerticle.class);

    private final Map<String, Vertxlet> vertxletMap = new HashMap<>();
    private ServerOptions options;

    public ServerVerticle(ServerOptions options) {
        this.options = options;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create());

        logger.info("Add TimeoutHandler with timeout: " + options.timeout() + " ms");
        router.route().handler(TimeoutHandler.create(options.timeout()));

        if (options.isEnableResponseTimeHandler()) {
            logger.debug("ResponseTimeHandler is enabled");
            router.route().handler(ResponseTimeHandler.create());
        }

        if (options.isEnableLoggerHandler()) {
            logger.debug("LoggerHandler is enabled");
            router.route().handler(LoggerHandler.create(false, LoggerHandler.Format.DEFAULT));
        }

        router.route().handler(InitializeHandler.create()).failureHandler(FailureHandler.create());

        try {
            scanVertxlet(router);
            RequestDispatcher.init(router);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                | InstantiationException | VertxletException e) {
            logger.error("Error when scan urls", e);
            startFuture.fail(e);
            return;
        }

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(options.port(), options.address(), res -> {
                    if (res.succeeded()) {
                        logger.info(String.format("Http server started at [%s:%d]",
                                options.address(), options.port()));
                        initializeVertxlet(startFuture);
                    } else {
                        startFuture.fail(res.cause());
                    }
                });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        AtomicInteger count = new AtomicInteger(vertxletMap.size());
        if (count.get() <= 0) {
            stopFuture.complete();
            return;
        }

        for (String key : vertxletMap.keySet()) {
            Future<Void> future = Future.future();
            vertx.getOrCreateContext().runOnContext(v -> vertxletMap.get(key).destroy(future));
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    if (count.decrementAndGet() == 0) {
                        logger.info("All vertxlet destroy succeeded");
                        stopFuture.complete();
                    }
                } else {
                    stopFuture.fail(ar.cause());
                }
            });
        }
    }

    private void scanVertxlet(Router router) throws Exception {
        final Reflections reflections = new Reflections("");
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(VertxletMapping.class)) {
            Vertxlet vertxlet;
            try {
                vertxlet = (Vertxlet) SimpleClassLoader.loadClass(clazz);
                vertxlet.setContext(vertx);
            } catch (ClassCastException e) {
                logger.error(null, e);
                continue;
            }

            for (String url : clazz.getAnnotation(VertxletMapping.class).url()) {
                logger.info(String.format("Mapping url %s with class %s", url, clazz.getName()));
                vertxletMap.put(url, vertxlet);

                if (clazz.isAnnotationPresent(Jdbc.class)) {
                    router.route(url).handler(DatabaseHandler.create(Jdbc.class));
                }

                if (clazz.isAnnotationPresent(Redis.class)) {
                    router.route(url).handler(DatabaseHandler.create(Redis.class));
                }

                router.route(url).handler(vertxlet::handle);
            }
        }
    }

    private void initializeVertxlet(Future<Void> fut) {
        AtomicInteger count = new AtomicInteger(vertxletMap.size());
        if (count.get() <= 0) {
            fut.complete();
            return;
        }

        for (String key : vertxletMap.keySet()) {
            Future<Integer> future = Future.future();
            vertx.runOnContext(v -> vertxletMap.get(key).init(future));
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    if (count.decrementAndGet() == 0) {
                        logger.info("All vertxlet init succeeded");
                        fut.complete();
                    }
                } else {
                    fut.fail(ar.cause());
                }
            });
        }
    }
}