package com.admicro.vertxlet.core;

import com.admicro.vertxlet.utils.TaskRunner;
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
import io.vertx.redis.RedisClient;

import java.util.ArrayList;
import java.util.List;

public class HttpVertxlet implements IHttpVertxlet {

    private static final Logger _logger = LoggerFactory.getLogger(HttpVertxlet.class);

    private Vertx vertx;
    private Verticle verticle;

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
        List<RunnableFuture<Void>> rfs = new ArrayList<>();
        rfs.add(fut -> { // create jdbc connection (if needed)
            if (getClass().isAnnotationPresent(Jdbc.class)) {
                JsonObject jdbcConfig = getDatabaseConfig().getJsonObject("jdbc");
                JDBCClient.createShared(vertx, jdbcConfig).getConnection(ar -> {
                    if (ar.failed()) {
                        routingContext.fail(ar.cause());
                    } else {
                        SQLConnection con = ar.result();
                        routingContext.put("jdbc", con);
                        routingContext.addHeadersEndHandler(fut2 -> con.close(v -> {
                            if (v.failed()) {
                                fut2.fail(v.cause());
                            } else {
                                fut2.complete();
                            }
                        }));
                        fut.complete();
                    }
                });
            } else {
                fut.complete();
            }
        });
        rfs.add(fut -> { // create redis connection (if need)
            if (getClass().isAnnotationPresent(Redis.class)) {
                JsonObject redisConfig = getDatabaseConfig().getJsonObject("redis");
                RedisClient redis = RedisClient.create(vertx, redisConfig);

                routingContext.put("redis", redis);
                routingContext.addHeadersEndHandler(fut2 -> redis.close(v -> {
                    if (v.failed()) {
                        fut2.fail(v.cause());
                    } else {
                        fut2.complete();
                    }
                }));
            } else {
                fut.complete();
            }
        });

        TaskRunner.executeParallel(rfs, ar -> routeByMethod(routingContext));
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

    protected void doPut(RoutingContext routingContext) throws Exception {
        routingContext.response().end();
    }

    protected void doHead(RoutingContext routingContext) throws Exception {
        routingContext.response().end();
    }

    protected void doDelete(RoutingContext routingContext) throws Exception {
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
        SQLConnection con = routingContext.get("jdbc");
        if (con == null) {
            VertxException e = new VertxException("Vertxlet was not declared with @Jdbc");
            _logger.error(e.getMessage(), e);
            throw e;
        }
        return con;
    }

    protected RedisClient getRedisClient(RoutingContext routingContext) throws  VertxException {
        RedisClient redis = routingContext.get("redis");
        if (redis == null) {
            VertxException e = new VertxException("Vertxlet was not declared with @Redis");
            _logger.error(e.getMessage(), e);
            throw e;
        }
        return redis;
    }

    protected void routeByMethod(RoutingContext routingContext) {
        try {
            if (routingContext.request().method() == HttpMethod.GET) {
                doGet(routingContext);
            } else if (routingContext.request().method() == HttpMethod.POST) {
                doPost(routingContext);
            } else if (routingContext.request().method() == HttpMethod.PUT) {
                doPut(routingContext);
            } else if (routingContext.request().method() == HttpMethod.HEAD) {
                doHead(routingContext);
            } else if (routingContext.request().method() == HttpMethod.DELETE) {
                doDelete(routingContext);
            } else {
                UnsupportedOperationException e = new UnsupportedOperationException("Method not support");
                _logger.error(e.getMessage(), e);
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