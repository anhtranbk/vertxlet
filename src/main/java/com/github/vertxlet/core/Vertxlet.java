package com.github.vertxlet.core;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;

public interface Vertxlet extends Handler<RoutingContext> {

    default <T> void init(Promise<T> promise) {
        promise.complete();
    }

    default <T> void destroy(Promise<T> promise) {
        promise.complete();
    }
}
