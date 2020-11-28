package com.vertxlet.core;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public interface Vertxlet {

    void vertx(Vertx vertx);

    Vertx vertx();

    default <T> void init(Promise<T> promise) {
        promise.complete();
    }

    default <T> void destroy(Promise<T> promise) {
        promise.complete();
    }

    void handle(RoutingContext routingContext);
}
