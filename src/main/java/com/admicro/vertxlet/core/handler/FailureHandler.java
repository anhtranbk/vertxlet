package com.admicro.vertxlet.core.handler;

import com.admicro.vertxlet.core.handler.impl.FailureHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface FailureHandler extends Handler<RoutingContext> {

    static FailureHandler create() {
        return new FailureHandlerImpl();
    }
}
