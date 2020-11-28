package com.github.vertxlet.core;

import com.github.vertxlet.core.impl.FailureHandlerImpl;
import com.github.vertxlet.core.impl.InitializeHandlerImpl;
import com.github.vertxlet.core.impl.VertxletContextImpl;
import com.github.vertxlet.util.ReflectionUtils;
import com.github.vertxlet.util.Config;
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

    private static final Logger logger = LoggerFactory.getLogger(ServerVerticle.class);
    private final Map<String, Vertxlet> vertxletMap = new HashMap<>();
    private final Config conf;

    public ServerVerticle(Config conf) {
        this.conf = conf;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(new InitializeHandlerImpl())
                .handler(BodyHandler.create())
                .failureHandler(new FailureHandlerImpl());

        ServerOptions options = new ServerOptions(this.conf);

        logger.info("Add TimeoutHandler with timeout {} ms", options.timeout());
        router.route().handler(TimeoutHandler.create(options.timeout()));

        if (options.isEnableResponseTimeHandler()) {
            logger.debug("ResponseTimeHandler is enabled");
            router.route().handler(ResponseTimeHandler.create());
        }

        if (options.isEnableLoggerHandler()) {
            logger.debug("LoggerHandler is enabled");
            router.route().handler(LoggerHandler.create(true, LoggerFormat.DEFAULT));
        }

        try {
            Context ctx = new VertxletContextImpl(vertx, conf);
            RequestDispatcher dispatcher = new RequestDispatcher(ctx, router);
            dispatcher.scanControllers();

            Map<String, Vertxlet> vertxletMap = dispatcher.scanVertxlet();
            this.vertxletMap.putAll(vertxletMap);
        } catch (Exception e) {
            logger.error("Error when scan urls", e);
            startPromise.fail(e);
            return;
        }

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(options.port(), options.address(), res -> {
                    if (res.succeeded()) {
                        logger.info("Http server started at [{}:{}]",
                                options.address(), options.port());
                        initializeVertxlet(startPromise);
                    } else {
                        startPromise.fail(res.cause());
                    }
                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        destroyVertxlet(stopPromise);
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

    private void destroyVertxlet(Promise<Void> stopPromise) {
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
}
