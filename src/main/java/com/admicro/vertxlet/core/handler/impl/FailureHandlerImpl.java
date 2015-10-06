package com.admicro.vertxlet.core.handler.impl;

import com.admicro.vertxlet.core.handler.FailureHandler;
import com.admicro.vertxlet.core.HttpServerVerticle;
import com.admicro.vertxlet.core.db.IDbConnector;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

public class FailureHandlerImpl implements FailureHandler {

    private static final Logger _logger = LoggerFactory.getLogger(FailureHandlerImpl.class);

    @Override
    public void handle(RoutingContext rc) {
        _logger.error("Unexpected error occur", rc.failure());

        // Guarantee db connections is closed when error occurs
        Map<String, IDbConnector> iDbAdaptorMap = rc.get(HttpServerVerticle.DATABASE_KEY);
        for (IDbConnector adaptor : iDbAdaptorMap.values()) {
            adaptor.close(v -> {
            });
        }

        rc.response().putHeader("content-type", "text/html")
                .setStatusCode(500).end("<html><h1>Server internal error</h1></html>");
    }
}
