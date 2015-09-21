package com.admicro.vertxlet.core.db;

import com.sun.istack.internal.NotNull;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public interface IDbConnector {

    void openConnection(@NotNull Vertx vertx, @NotNull JsonObject config,
                        @NotNull Handler<AsyncResult<Void>> handler);
    <T> T getInstance();
    void close(Handler<AsyncResult<Void>> handler);

    default void close() {
        close(ar -> {
        });
    }

    static final IllegalStateException NOT_INITIALIZED_EXCEPTION = new IllegalStateException(
            "Database instance has not been initialized");
}
