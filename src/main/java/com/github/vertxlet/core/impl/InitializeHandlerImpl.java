package com.github.vertxlet.core.impl;

import com.github.vertxlet.core.Constants;
import com.github.vertxlet.core.DatabaseConnection;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

public class InitializeHandlerImpl implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext rc) {
        // Map for save DatabaseConnection instances, use for clean up
        Map<String, DatabaseConnection<?>> connectionMap = new HashMap<>();
        rc.put(Constants.DATABASE_KEY, connectionMap);
        rc.next();
    }
}
