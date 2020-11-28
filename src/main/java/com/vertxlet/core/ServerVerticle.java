package com.vertxlet.core;

import com.vertxlet.core.handler.FailureHandler;
import com.vertxlet.core.handler.InitializeHandler;
import com.vertxlet.core.spring.RequestDispatcher;
import com.vertxlet.util.ClassLoaders;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerVerticle extends AbstractVerticle {

    public static final String DEFAULT_SHARE_LOCAL_MAP = ServerVerticle.class.getName();
    private static final Logger logger = LoggerFactory.getLogger(ServerVerticle.class);

    private final Map<String, Vertxlet> vertxletMap = new HashMap<>();
    private final ServerOptions options;

    public ServerVerticle(ServerOptions options) {
        this.options = options;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(InitializeHandler.create())
                .handler(BodyHandler.create())
                .failureHandler(FailureHandler.create());

        logger.info("Add TimeoutHandler with timeout: " + options.timeout() + " ms");
        router.route().handler(TimeoutHandler.create(options.timeout()));

        if (options.isEnableResponseTimeHandler()) {
            logger.debug("ResponseTimeHandler is enabled");
            router.route().handler(ResponseTimeHandler.create());
        }

        if (options.isEnableLoggerHandler()) {
            logger.debug("LoggerHandler is enabled");
            router.route().handler(LoggerHandler.create(false, LoggerFormat.DEFAULT));
        }

        try {
            scanVertxlet(router);
            RequestDispatcher.init(router);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                | InstantiationException | VertxletException e) {
            logger.error("Error when scan urls", e);
            startPromise.fail(e);
            return;
        }

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(options.port(), options.address(), res -> {
                    if (res.succeeded()) {
                        logger.info(String.format("Http server started at [%s:%d]",
                                options.address(), options.port()));
                        initializeVertxlet(startPromise);
                    } else {
                        startPromise.fail(res.cause());
                    }
                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        AtomicInteger count = new AtomicInteger(vertxletMap.size());
        if (count.get() <= 0) {
            stopPromise.complete();
            return;
        }

        vertxletMap.forEach((name, vertxlet) -> {
            Promise<Void> promise = Promise.promise();
            vertx.runOnContext(v -> vertxlet.destroy(promise));
            promise.future().onComplete(ar -> {
                if (ar.succeeded()) {
                    if (count.decrementAndGet() == 0) {
                        logger.info("All vertxlet destroy succeeded");
                        stopPromise.complete();
                    }
                } else {
                    stopPromise.fail(ar.cause());
                }
            });
        });
    }

    private void scanVertxlet(Router router) throws Exception {
        final Reflections reflections = new Reflections("");
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(VertxletMapping.class)) {
            Vertxlet vertxlet;
            try {
                vertxlet = (Vertxlet) ClassLoaders.loadClass(clazz);
                vertxlet.vertx(vertx);
            } catch (ClassCastException e) {
                logger.error("Load class failed " + clazz.getName(), e);
                continue;
            }

            for (String url : clazz.getAnnotation(VertxletMapping.class).url()) {
                logger.info(String.format("Mapping url %s with class %s", url, clazz.getName()));
                vertxletMap.put(url, vertxlet);

                router.route(url).handler(vertxlet::handle);
            }
        }
    }

    private void initializeVertxlet(Promise<Void> startPromise) {
        AtomicInteger count = new AtomicInteger(vertxletMap.size());
        if (count.get() == 0) {
            startPromise.complete();
            return;
        }

        for (String key : vertxletMap.keySet()) {
            Promise<Void> promise = Promise.promise();
            vertx.runOnContext(v -> vertxletMap.get(key).init(promise));
            promise.future().onComplete(ar -> {
                if (ar.succeeded()) {
                    if (count.decrementAndGet() == 0) {
                        logger.info("All vertxlet initialized");
                        startPromise.complete();
                    }
                } else {
                    startPromise.fail(ar.cause());
                }
            });
        }
    }
}
