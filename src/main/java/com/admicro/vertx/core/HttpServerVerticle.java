package com.admicro.vertx.core;

import com.admicro.vertx.utils.SimpleClassLoader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class HttpServerVerticle extends AbstractVerticle {

    static final String ROOT_PATH = "/*";
    static final int DEFAULT_HTTP_PORT = 8888;
    static final String WAN_ADDRESS = "0.0.0.0";

    private final Map<String, IHttpVertxlet> mappingUrls = new HashMap<>();
    private Logger logger;

    public HttpServerVerticle(ServerOptions options) {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public HttpServerVerticle() {
        this(null);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);

        router.route(ROOT_PATH).handler(routingContext -> {
            routingContext.addHeadersEndHandler(Future::complete);
            routingContext.next();
        }).failureHandler(routingContext -> routingContext.response().end("404"));

        try {
            scanForMappingUrl(router);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | InstantiationException e) {
            logger = LoggerFactory.getLogger(HttpServerVerticle.class);
            logger.error(e.getMessage(), e);
            startFuture.fail(e);
            return;
        }

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router::accept).listen(DEFAULT_HTTP_PORT, WAN_ADDRESS, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        stopFuture.complete();
    }

    private void scanForMappingUrl(Router router) throws Exception {
        final Reflections reflections = new Reflections("");

        for (Class<?> clazz : reflections.getSubTypesOf(IHttpVertxlet.class)) {
            if (!clazz.isAnnotationPresent(VertxServlet.class)) continue;

            for (String url : clazz.getAnnotation(VertxServlet.class).url()) {
                IHttpVertxlet servlet;
                if (!mappingUrls.containsKey(url)) {
                    servlet = (IHttpVertxlet) SimpleClassLoader.loadClass(clazz);
                    servlet.setContext(vertx, this);

                    System.out.println(String.format("Mapping url %s with class %s", url, clazz.getName()));
                    mappingUrls.put(url, servlet);
                } else {
                    servlet = mappingUrls.get(url);
                }

                router.route(url).handler(servlet::handle);
            }
        }
    }
}
