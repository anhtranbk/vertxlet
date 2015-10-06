package com.admicro.vertxlet.core.db.impl;

import com.admicro.vertxlet.core.HttpServerVerticle;
import com.admicro.vertxlet.core.db.DatabaseHandler;
import com.admicro.vertxlet.core.db.IDbConnector;
import com.admicro.vertxlet.core.db.impl.DbConnectorFactory;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

public class DatabaseHandlerImpl implements DatabaseHandler {

    private String dbName;

    public DatabaseHandlerImpl(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public void handle(RoutingContext rc) {
        IDbConnector adaptor = DbConnectorFactory.iDbAdaptor(dbName);
        JsonObject config = getDatabaseConfig(rc.vertx()).getJsonObject(dbName.toLowerCase());

        Future<Void> future = Future.future();
        future.setHandler(ar -> {
            if (ar.failed()) {
                rc.fail(ar.cause());
            } else {
                rc.next();
            }
        });

        adaptor.openConnection(rc.vertx(), config, ar -> {
            if (ar.failed()) {
                future.fail(ar.cause());
            } else {
                Map<String, IDbConnector> map = rc.get(HttpServerVerticle.DATABASE_KEY);
                map.put(dbName, adaptor);
                rc.addHeadersEndHandler(fut -> { // close db connection when process done
                    adaptor.close(v -> {
                    });
                    fut.complete();
                });
                future.complete();
            }
        });
    }

    private JsonObject getDatabaseConfig(Vertx vertx) {
        return (JsonObject) vertx.sharedData().getLocalMap(
                HttpServerVerticle.DEFAULT_SHARE_LOCAL_MAP).get("db_options");
    }
}
