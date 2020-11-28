package com.vertxlet.core.handler.impl;

import com.vertxlet.core.Constants;
import com.vertxlet.core.db.DatabaseConnection;
import com.vertxlet.core.handler.InitializeHandler;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

public class InitializeHandlerImpl implements InitializeHandler {

    @Override
    public void handle(RoutingContext rc) {
        // Map for save DatabaseConnection instances, use for clean up
        Map<String, DatabaseConnection<?>> connectionMap = new HashMap<>();
        rc.put(Constants.DATABASE_KEY, connectionMap);
        rc.next();
    }
}
