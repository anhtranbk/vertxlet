package com.admicro.vertxlet.core;

import com.admicro.vertxlet.core.db.IDbConnector;
import com.admicro.vertxlet.core.db.Jdbc;
import com.admicro.vertxlet.core.db.Redis;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;

import java.util.Map;

public class HttpVertxlet implements IHttpVertxlet {

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
                throw new UnsupportedOperationException("Method not support");
            }
        } catch (Exception e) {
            if (e instanceof UnsupportedOperationException) {
                throw (UnsupportedOperationException) e;
            } else {
                rc.fail(e);
            }
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

    protected SQLConnection getSqlConnection(RoutingContext rc) throws NullPointerException {
        Map<String, IDbConnector> map = rc.get(HttpServerVerticle.DATABASE_KEY);
        return map.get(Jdbc.class.getSimpleName()).getInstance();
    }

    protected RedisClient getRedisClient(RoutingContext rc) throws NullPointerException {
        Map<String, IDbConnector> map = rc.get(HttpServerVerticle.DATABASE_KEY);
        return map.get(Redis.class.getSimpleName()).getInstance();
    }

    protected final JsonObject getDatabaseConfig() {
        return (JsonObject) vertx.sharedData().getLocalMap(
                HttpServerVerticle.DEFAULT_SHARE_LOCAL_MAP).get("db_options");
    }

    protected <T> void executeBlocking(Handler<Future<T>> blockingHandler,
                                       Handler<AsyncResult<T>> resultHandler) {
        vertx.executeBlocking(blockingHandler, false, resultHandler);
    }

    protected <T> void executeBlocking(Handler<Future<T>> blockingHandler, boolean ordered,
                                       Handler<AsyncResult<T>> resultHandler) {
        vertx.executeBlocking(blockingHandler, ordered, resultHandler);
    }
}