package com.admicro.vertxlet.core.handler.impl;

import com.admicro.vertxlet.core.HttpServerVerticle;
import com.admicro.vertxlet.core.handler.InitializeHandler;
import com.admicro.vertxlet.core.db.IDbConnector;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

public class InitializeHandlerImpl implements InitializeHandler {

    @Override
    public void handle(RoutingContext rc) {
        // Map for save IDbAdaptor instances, using for clean up
        Map<String, IDbConnector> iDbAdaptorMap = new HashMap<>();
        rc.put(HttpServerVerticle.DATABASE_KEY, iDbAdaptorMap);

        rc.addHeadersEndHandler(Future::complete);
        rc.next();
    }
}
