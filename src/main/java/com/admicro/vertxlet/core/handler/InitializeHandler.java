package com.admicro.vertxlet.core.handler;

import com.admicro.vertxlet.core.handler.impl.InitializeHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface InitializeHandler extends Handler<RoutingContext> {

    static InitializeHandler create() {
        return new InitializeHandlerImpl();
    }
}
