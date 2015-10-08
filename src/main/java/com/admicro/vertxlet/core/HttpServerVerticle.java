package com.admicro.vertxlet.core;

import com.admicro.vertxlet.core.db.DatabaseHandler;
import com.admicro.vertxlet.core.db.Jdbc;
import com.admicro.vertxlet.core.db.Redis;
import com.admicro.vertxlet.core.handler.FailureHandler;
import com.admicro.vertxlet.core.handler.InitializeHandler;
import com.admicro.vertxlet.util.SimpleClassLoader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
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

public class HttpServerVerticle extends AbstractVerticle {

    public static final String DATABASE_KEY = "db";
    public static final String DEFAULT_SHARE_LOCAL_MAP = HttpServerVerticle.class.getName();

    static final Logger _logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    private final Map<String, IHttpVertxlet> mappingUrls = new HashMap<>();
    private ServerOptions options;

    public HttpServerVerticle(ServerOptions options) {
        this.options = options;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create());

        _logger.info("Add TimeoutHandler with timeout: " + options.timeout() + " ms");
        router.route().handler(TimeoutHandler.create(options.timeout()));

        if (options.isEnableReponseTimeHandler()) {
            _logger.debug("ResponseTimeHandler is enabled");
            router.route().handler(ResponseTimeHandler.create());
        }

        if (options.isEnableLoggerHandler()) {
            _logger.debug("LoggerHandler is enabled");
            router.route().handler(LoggerHandler.create(false, LoggerHandler.Format.DEFAULT));
        }

        router.route().handler(InitializeHandler.create()).failureHandler(FailureHandler.create());

        try {
            scanForMappingUrl(router);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | InstantiationException e) {
            _logger.error("Error when scan urls", e);
            startFuture.fail(e);
            return;
        }

        Future<Void> initFuture = Future.future();
        initFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                HttpServer server = vertx.createHttpServer();
                server.requestHandler(router::accept).listen(options.port(), options.address(), res -> {
                    if (res.succeeded()) {
                        _logger.info(String.format("Http server started at [%s:%d]",
                                options.address(), options.port()));
                        startFuture.complete();
                    } else {
                        startFuture.fail(res.cause());
                    }
                });
            } else {
                startFuture.fail(ar.cause());
            }
        });
        callInitVertxlet(initFuture);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        AtomicInteger count = new AtomicInteger(mappingUrls.size());
        if (count.get() <= 0) {
            stopFuture.complete();
            return;
        }

        for (String key : mappingUrls.keySet()) {
            Future<Void> future = Future.future();
            vertx.getOrCreateContext().runOnContext(v -> mappingUrls.get(key).destroy(future));
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    if (count.decrementAndGet() == 0) {
                        _logger.info("All vertxlet destroy succeeded");
                        stopFuture.complete();
                    }
                } else {
                    stopFuture.fail(ar.cause());
                }
            });
        }
    }

    private void scanForMappingUrl(Router router) throws Exception {
        final Reflections reflections = new Reflections("");
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(RequestMapping.class)) {
            IHttpVertxlet servlet;
            try {
                servlet = (IHttpVertxlet) SimpleClassLoader.loadClass(clazz);
                servlet.setContext(vertx, this);
            } catch (ClassCastException e) {
                _logger.error(null, e);
                continue;
            }

            for (String url : clazz.getAnnotation(RequestMapping.class).url()) {
                _logger.info(String.format("Mapping url %s with class %s", url, clazz.getName()));
                mappingUrls.put(url, servlet);

                if (clazz.isAnnotationPresent(Jdbc.class)) {
                    router.route(url).handler(DatabaseHandler.create(Jdbc.class));
                }

                if (clazz.isAnnotationPresent(Redis.class)) {
                    router.route(url).handler(DatabaseHandler.create(Redis.class));
                }

                router.route(url).handler(servlet::handle);
            }
        }
    }

    private void callInitVertxlet(Future<Void> initFuture) {
        AtomicInteger count = new AtomicInteger(mappingUrls.size());
        if (count.get() <= 0) {
            initFuture.complete();
            return;
        }

        for (String key : mappingUrls.keySet()) {
            Future<Integer> future = Future.future();
            vertx.getOrCreateContext().runOnContext(v -> mappingUrls.get(key).init(future));
            future.setHandler(ar -> {
                if (ar.succeeded()) {
                    if (count.decrementAndGet() == 0) {
                        _logger.info("All vertxlet init succeeded");
                        initFuture.complete();
                    }
                } else {
                    initFuture.fail(ar.cause());
                }
            });
        }
    }
}