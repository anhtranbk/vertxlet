package com.admicro.vertxlet.core;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public interface IHttpVertxlet {

    void setContext(Vertx vertx);

    default <T> void init(Future<T> future) {
        future.complete();
    }

    default <T> void destroy(Future<T> future) {
        future.complete();
    }

    void handle(RoutingContext routingContext);

    Vertx getVertx();
}
