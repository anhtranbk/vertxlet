package com.admicro.vertxlet.core.db.impl;

import com.admicro.vertxlet.core.db.DbConnector;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

public class RedisConnector implements DbConnector {

    private RedisClient redis;

    @Override
    public void openConnection(Vertx vertx, JsonObject config,
                               Handler<AsyncResult<Void>> handler) {
        redis = RedisClient.create(vertx, config);
        handler.handle(Future.succeededFuture());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance() {
        if (redis == null)
            throw DbConnector.NOT_INITIALIZED_EXCEPTION;
        return (T) redis;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        if (redis != null) {
            redis.close(handler);
        } else {
            handler.handle(Future.succeededFuture());
        }
    }
}
