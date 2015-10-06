package com.admicro.vertxlet.core.db;

import com.admicro.vertxlet.core.db.impl.DatabaseHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface DatabaseHandler extends Handler<RoutingContext> {

    static DatabaseHandler create(String dbName) {
        return new DatabaseHandlerImpl(dbName);
    }
}
