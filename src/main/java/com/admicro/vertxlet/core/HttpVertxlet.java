package com.admicro.vertxlet.core;

import com.admicro.vertxlet.core.db.DbAdaptorFactory;
import com.admicro.vertxlet.core.db.IDbAdaptor;
import com.admicro.vertxlet.core.db.Jdbc;
import com.admicro.vertxlet.core.db.Redis;
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
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        routingContext.put("start", System.currentTimeMillis());
        List<RunnableFuture<Void>> rfs = new ArrayList<>();
        if (getClass().isAnnotationPresent(Jdbc.class)) {
            rfs.add(fut -> setupDatabase(routingContext, Jdbc.class.getSimpleName(), fut));
        }
        if (getClass().isAnnotationPresent(Redis.class)) {
            rfs.add(fut -> setupDatabase(routingContext, Redis.class.getSimpleName(), fut));
        }

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
        Map<String, IDbAdaptor> map = routingContext.get(HttpServerVerticle.DATABASE_KEY);
        SQLConnection con = map.get(Jdbc.class.getSimpleName()).getInstance();
        if (con == null) {
            VertxException e = new VertxException("Vertxlet was not declared with @Jdbc");
            _logger.error(e.getMessage(), e);
            throw e;
        }
        return con;
    }

    protected RedisClient getRedisClient(RoutingContext routingContext) throws VertxException {
        Map<String, IDbAdaptor> map = routingContext.get(HttpServerVerticle.DATABASE_KEY);
        RedisClient redis = map.get(Redis.class.getSimpleName()).getInstance();
        if (redis == null) {
            VertxException e = new VertxException("Vertxlet was not declared with @Redis");
            _logger.error(e.getMessage(), e);
            throw e;
        }
        return redis;
    }

    protected final JsonObject getDatabaseConfig() {
        return (JsonObject) vertx.sharedData().getLocalMap(
                HttpServerVerticle.DEFAULT_SHARE_LOCAL_MAP).get("db_options");
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

    private void setupDatabase(RoutingContext routingContext, String type, Future<Void> future) {
        IDbAdaptor adaptor = DbAdaptorFactory.iDbAdaptor(type);
        JsonObject config = getDatabaseConfig().getJsonObject(type.toLowerCase());

        adaptor.openConnection(vertx, config, ar -> {
            if (ar.failed()) {
                _logger.error("Open database connection failed", ar.cause());
                routingContext.fail(ar.cause());
            } else {
                Map<String, IDbAdaptor> map = routingContext.get(HttpServerVerticle.DATABASE_KEY);
                map.put(type, adaptor);
                routingContext.addHeadersEndHandler(fut -> {
                    adaptor.close(v -> {
                    });
                    fut.complete();
                });
                future.complete();
            }
        });
    }
}