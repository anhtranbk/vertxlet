package com.admicro.vertxlet.core.handler.impl;

import com.admicro.vertxlet.core.ServerVerticle;
import com.admicro.vertxlet.core.handler.InitializeHandler;
import com.admicro.vertxlet.core.db.DbConnector;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

public class InitializeHandlerImpl implements InitializeHandler {

    @Override
    public void handle(RoutingContext rc) {
        // Map for save IDbAdaptor instances, using for clean up
        Map<String, DbConnector> iDbAdaptorMap = new HashMap<>();
        rc.put(ServerVerticle.DATABASE_KEY, iDbAdaptorMap);

        rc.addHeadersEndHandler(Future::complete);
        rc.next();
    }
}
