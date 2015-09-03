package com.admicro.vertx.examples;

import com.admicro.vertx.core.HttpVertxlet;
import com.admicro.vertx.core.VertxletRequestMapping;
import io.vertx.ext.web.RoutingContext;

@VertxletRequestMapping(url = {"/delay/:delay"})
public class TestDelayVertxlet extends HttpVertxlet {

    @Override
    protected void doGet(RoutingContext routingContext) throws Exception {
        System.out.println("Received request from: " + routingContext.request().path());
        postDelay(() -> routingContext.response().end("Task delay executed"),
                Long.parseLong(routingContext.request().getParam("delay")));
    }

    @Override
    protected void doPost(RoutingContext routingContext) throws Exception {
        runOnSameContext(() -> routingContext.response().end("Test runOnSameContext succeeded"));
    }
}
