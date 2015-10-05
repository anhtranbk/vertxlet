package com.admicro.vertxlet.core.impl;

import com.admicro.vertxlet.core.IHttpVertxlet;
import com.admicro.vertxlet.core.RunnableFuture;
import com.admicro.vertxlet.core.db.IDbConnector;
import com.admicro.vertxlet.core.db.Jdbc;
import com.admicro.vertxlet.core.db.Redis;
import com.admicro.vertxlet.core.db.impl.DbConnectorFactory;
import com.admicro.vertxlet.util.TaskRunner;
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
    public void handle(RoutingContext rc) {
        List<RunnableFuture<Void>> rfs = new ArrayList<>();
        if (getClass().isAnnotationPresent(Jdbc.class)) {
            rfs.add(fut -> setupDatabase(rc, Jdbc.class.getSimpleName(), fut));
        }
        if (getClass().isAnnotationPresent(Redis.class)) {
            rfs.add(fut -> setupDatabase(rc, Redis.class.getSimpleName(), fut));
        }

        if(!rfs.isEmpty()) {
            TaskRunner.executeParallel(rfs, ar -> {
                if (ar.failed()) {
                    rc.fail(ar.cause());
                } else {
                    routeByMethod(rc);
                }
            });
        } else {
            routeByMethod(rc);
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

    protected void doGet(RoutingContext rc) throws Exception {
        rc.response().end();
    }

    protected void doPost(RoutingContext rc) throws Exception {
        rc.response().end();
    }

    protected void doPut(RoutingContext rc) throws Exception {
        rc.response().end();
    }

    protected void doHead(RoutingContext rc) throws Exception {
        rc.response().end();
    }

    protected void doDelete(RoutingContext rc) throws Exception {
        rc.response().end();
    }

    protected SQLConnection getSqlConnection(RoutingContext rc) throws VertxException {
        Map<String, IDbConnector> map = rc.get(HttpServerVerticle.DATABASE_KEY);
        SQLConnection con = map.get(Jdbc.class.getSimpleName()).getInstance();
        if (con == null) {
            VertxException e = new VertxException("Vertxlet was not declared with @Jdbc");
            _logger.error(e.getMessage(), e);
            throw e;
        }
        return con;
    }

    protected RedisClient getRedisClient(RoutingContext rc) throws VertxException {
        Map<String, IDbConnector> map = rc.get(HttpServerVerticle.DATABASE_KEY);
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

    protected void routeByMethod(RoutingContext rc) {
        try {
            if (rc.request().method() == HttpMethod.GET) {
                doGet(rc);
            } else if (rc.request().method() == HttpMethod.POST) {
                doPost(rc);
            } else if (rc.request().method() == HttpMethod.PUT) {
                doPut(rc);
            } else if (rc.request().method() == HttpMethod.HEAD) {
                doHead(rc);
            } else if (rc.request().method() == HttpMethod.DELETE) {
                doDelete(rc);
            } else {
                UnsupportedOperationException e = new UnsupportedOperationException("Method not support");
                _logger.error(e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            if (e instanceof UnsupportedOperationException) {
                throw (UnsupportedOperationException) e;
            } else {
                rc.fail(e);
            }
        }
    }

    protected <T> void executeBlocking(Handler<Future<T>> blockingHandler,
                                       Handler<AsyncResult<T>> resultHandler) {
        vertx.executeBlocking(blockingHandler, false, resultHandler);
    }

    private void setupDatabase(RoutingContext rc, String type, Future<Void> future) {
        IDbConnector adaptor = DbConnectorFactory.iDbAdaptor(type);
        JsonObject config = getDatabaseConfig().getJsonObject(type.toLowerCase());

        adaptor.openConnection(vertx, config, ar -> {
            if (ar.failed()) {
                future.fail(ar.cause());
            } else {
                Map<String, IDbConnector> map = rc.get(HttpServerVerticle.DATABASE_KEY);
                map.put(type, adaptor);
                rc.addHeadersEndHandler(fut -> {
                    adaptor.close(v -> {
                    });
                    fut.complete();
                });
                future.complete();
            }
        });
    }
}