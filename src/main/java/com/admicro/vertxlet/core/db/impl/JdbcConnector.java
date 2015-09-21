package com.admicro.vertxlet.core.db.impl;

import com.admicro.vertxlet.core.db.IDbConnector;
import com.sun.istack.internal.NotNull;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class JdbcConnector implements IDbConnector {

    private SQLConnection con;

    @Override
    public void openConnection(@NotNull Vertx vertx, @NotNull JsonObject config,
                               @NotNull Handler<AsyncResult<Void>> handler) {

        JDBCClient.createShared(vertx, config).getConnection(ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
            } else {
                con = ar.result();
                handler.handle(Future.succeededFuture());
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance() {
        if (con == null)
            throw IDbConnector.NOT_INITIALIZED_EXCEPTION;
        return (T) con;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        if (con != null) {
            con.close(handler);
        } else {
            handler.handle(Future.succeededFuture());
        }
    }
}
