package com.admicro.vertx.core;

import com.admicro.vertx.utils.SimpleClassLoader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpServerVerticle extends AbstractVerticle {

    static final String ROOT_PATH = "/*";
    static final String DEFAULT_SHARE_LOCAL_MAP = HttpServerVerticle.class.getName();

    private final Map<String, Vertxlet> mappingUrls = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServerOptions options;

    public HttpServerVerticle(ServerOptions options) {
        this.options = options;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);

        router.route(ROOT_PATH).handler(routingContext -> {
            routingContext.addHeadersEndHandler(Future::complete);
            routingContext.next();
        }).failureHandler(routingContext -> {
            SQLConnection con = routingContext.get("db");
            if (con != null) {
                con.close(v -> {
                });
            }
            routingContext.response().putHeader("content-type", "text/html")
                    .setStatusCode(500).end("<html><h1>Server internal error</h1></html>");
        });

        try {
            scanForMappingUrl(router);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | InstantiationException e) {
            logger.error(e.getMessage(), e);
            startFuture.fail(e);
            return;
        }

        Future<Void> initFuture = Future.future();
        initFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                HttpServer server = vertx.createHttpServer();
                server.requestHandler(router::accept).listen(options.getPort(), options.getAddress(), res -> {
                    if (res.succeeded()) {
                        logger.info(String.format("Http server started at [%s:%d]",
                                options.getAddress(), options.getPort()));
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
                        logger.info("All vertxlet destroy succeeded");
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

        for (Class<?> clazz : reflections.getSubTypesOf(Vertxlet.class)) {
            if (!clazz.isAnnotationPresent(VertxletRequestMapping.class)) continue;

            for (String url : clazz.getAnnotation(VertxletRequestMapping.class).url()) {
                Vertxlet servlet = (Vertxlet) SimpleClassLoader.loadClass(clazz);
                servlet.setContext(vertx, this);

                logger.info(String.format("Mapping url %s with class %s", url, clazz.getName()));
                mappingUrls.put(url, servlet);

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
                        logger.info("All vertxlet init succeeded");
                        initFuture.complete();
                    }
                } else {
                    initFuture.fail(ar.cause());
                }
            });
        }
    }
}
