package com.admicro.vertx.core;

import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public interface Vertxlet {

    void setContext(Vertx vertx, Verticle verticle);

    default void init(Future<Void> future) throws Exception {
        future.complete();
    }

    default void destroy(Future<Void> future) throws Exception {
        future.complete();
    }

    void handle(RoutingContext routingContext);

    Vertx getVertx();

    Verticle getVerticle();
}
