package com.admicro.vertx.core;

import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public interface IHttpVertxlet {

    void setContext(Vertx vertx, Verticle verticle);

    void init(Future<Void> future) throws Exception;

    void destroy(Future<Void> future) throws Exception;

    void handle(RoutingContext routingContext);

    Vertx getVertx();

    Verticle getVerticle();
}
