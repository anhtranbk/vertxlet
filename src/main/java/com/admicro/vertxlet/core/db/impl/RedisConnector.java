package com.admicro.vertxlet.core.db.impl;

import com.admicro.vertxlet.core.db.IDbConnector;
import com.sun.istack.internal.NotNull;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

public class RedisConnector implements IDbConnector {

    private RedisClient redis;

    @Override
    public void openConnection(@NotNull Vertx vertx, @NotNull JsonObject config,
                               @NotNull Handler<AsyncResult<Void>> handler) {
        redis = RedisClient.create(vertx, config);
        handler.handle(Future.succeededFuture());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance() {
        if (redis == null)
            throw IDbConnector.NOT_INITIALIZED_EXCEPTION;
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
