package com.admicro.vertxlet.core.db.impl;

import com.admicro.vertxlet.core.ServerVerticle;
import com.admicro.vertxlet.core.db.DatabaseHandler;
import com.admicro.vertxlet.core.db.DbConnector;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.lang.annotation.Annotation;
import java.util.Map;

public class DatabaseHandlerImpl implements DatabaseHandler {

    private Class<? extends Annotation> clazz;

    public DatabaseHandlerImpl(Class<? extends Annotation> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void handle(RoutingContext rc) {
        String className = clazz.getSimpleName();
        DbConnector adaptor = DbConnector.create(clazz);
        if (adaptor == null) return;

        Future<Void> future = Future.future();
        future.setHandler(ar -> {
            if (ar.failed()) {
                rc.fail(ar.cause());
            } else {
                rc.next();
            }
        });

        JsonObject config = getDatabaseConfig(rc.vertx()).getJsonObject(className.toLowerCase());
        adaptor.openConnection(rc.vertx(), config, ar -> {
            if (ar.failed()) {
                future.fail(ar.cause());
            } else {
                Map<String, DbConnector> map = rc.get(ServerVerticle.DATABASE_KEY);
                map.put(className, adaptor);
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
                ServerVerticle.DEFAULT_SHARE_LOCAL_MAP).get("db_options");
    }
}
