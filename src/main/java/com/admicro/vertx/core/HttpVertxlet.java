package com.admicro.vertx.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

public class HttpVertxlet implements Vertxlet {

    private Vertx vertx;
    private Verticle verticle;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void setContext(Vertx vertx, Verticle verticle) {
        this.vertx = vertx;
        this.verticle = verticle;
    }

    @Override
    public <T> void init(Future<T> future) {
        try {
            init();
            future.complete();
        } catch (Exception e) {
            future.fail(e);
        }
    }

    @Override
    public <T> void destroy(Future<T> future) {
        try {
            destroy();
            future.complete();
        } catch (Exception e) {
            future.fail(e);
        }
    }

    @Override
    public void handle(RoutingContext routingContext) {
        if (getClass().getAnnotation(VertxletRequestMapping.class).usingDatabase()) {
            JDBCClient client = JDBCClient.createShared(vertx, getDatabaseConfig());

            client.getConnection(ar -> {
                if (ar.failed()) {
                    routingContext.fail(ar.cause());
                } else {
                    SQLConnection con = ar.result();
                    routingContext.put("db", con);
                    routingContext.addHeadersEndHandler(future -> con.close(v -> {
                        if (v.failed()) {
                            future.fail(v.cause());
                        } else {
                            future.complete();
                        }
                    }));
                    routeByMethod(routingContext);
                }
            });
        } else {
            routeByMethod(routingContext);
        }
    }

    @Override
    public final Vertx getVertx() {
        return vertx;
    }

    public final Verticle getVerticle() {
        return verticle;
    }

    protected void init() throws Exception {
    }

    protected void destroy() throws Exception {
    }

    protected void doGet(RoutingContext routingContext) throws Exception {
        routingContext.response().end();
    }

    protected void doPost(RoutingContext routingContext) throws Exception {
        routingContext.response().end();
    }

    protected <T> void executingHeavyTask(AsyncTask<T> task, Handler<AsyncResult<T>> handler) {
        executingHeavyTask(task, handler, false);
    }

    protected <T> void executingHeavyTask(AsyncTask<T> task, Handler<AsyncResult<T>> handler, boolean ordered) {
        vertx.executeBlocking(future -> {
            try {
                T result = task.doTask();
                future.complete(result);
            } catch (Exception e) {
                future.fail(e);
            }
        }, ordered, handler);
    }

    protected void runOnSameEventLoop(Runnable runnable) {
        vertx.getOrCreateContext().runOnContext(v -> runnable.run());
    }

    protected void runDelay(Runnable runnable, long delay) {
        vertx.setTimer(delay, id -> runnable.run());
    }

    protected SQLConnection getSqlConnection(RoutingContext routingContext) throws VertxException {

        SQLConnection con = routingContext.get("db");
        if (con == null) {
            VertxException e = new VertxException("Vertxlet was not declared using database");
            logger.error(e.getMessage(), e);
            throw e;
        }
        return con;
    }

    protected void routeByMethod(RoutingContext routingContext) {
        try {
            if (routingContext.request().method() == HttpMethod.GET) {
                doGet(routingContext);
            } else if (routingContext.request().method() == HttpMethod.POST) {
                doPost(routingContext);
            } else {
                UnsupportedOperationException e = new UnsupportedOperationException("Method not support");
                logger.error(e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            if (e instanceof UnsupportedOperationException) {
                throw (UnsupportedOperationException) e;
            } else {
                routingContext.fail(e);
            }
        }
    }

    private JsonObject getDatabaseConfig() {
        return (JsonObject) vertx.sharedData().getLocalMap(
                HttpServerVerticle.DEFAULT_SHARE_LOCAL_MAP).get("db_options");
    }
}