package com.admicro.vertx.examples;

import com.admicro.vertx.core.HttpVertxlet;
import com.admicro.vertx.core.VertxServlet;
import io.vertx.ext.web.RoutingContext;

@VertxServlet(url = {"/hello/:id", "/dspFake/dsp"})
public class HelloVertxlet extends HttpVertxlet {

    @Override
    protected void doGet(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id != null) {
            routingContext.response().end("id=" + id);
        } else {
            routingContext.response().end("Hello, current path: " + routingContext.request().path());
        }
    }

    @Override
    protected void doPost(RoutingContext routingContext) {
        super.doPost(routingContext);
    }
}
