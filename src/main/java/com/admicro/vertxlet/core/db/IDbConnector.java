package com.admicro.vertxlet.core.db;

import com.admicro.vertxlet.core.db.impl.JdbcConnector;
import com.admicro.vertxlet.core.db.impl.RedisConnector;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.lang.annotation.Annotation;

public interface IDbConnector {

    void openConnection(Vertx vertx, JsonObject config,
                        Handler<AsyncResult<Void>> handler);
    <T> T getInstance();
    void close(Handler<AsyncResult<Void>> handler);

    default void close() {
        close(ar -> {
        });
    }

    static final IllegalStateException NOT_INITIALIZED_EXCEPTION = new IllegalStateException(
            "Database instance has not been initialized");

    static IDbConnector create(Class<? extends Annotation> clazz) {
        if (Jdbc.class.equals(clazz)) {
            return new JdbcConnector();
        } else if (Redis.class.equals(clazz)) {
            return new RedisConnector();
        } else return null;
    }
}
