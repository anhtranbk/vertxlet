package examples;

import com.admicro.vertxlet.core.AbstractVertxlet;
import com.admicro.vertxlet.core.VertxletMapping;
import io.vertx.ext.web.RoutingContext;

@VertxletMapping(url = {"/delay/:delay"})
public class DelayExamples extends AbstractVertxlet {

    @Override
    protected void doGet(RoutingContext routingContext) throws Exception {
        long delay = Long.parseLong(routingContext.request().getParam("delay"));
        getVertx().setTimer(delay, ar -> routingContext.response().end("=>" + delay + "\r\n"));
    }

    @Override
    protected void doPost(RoutingContext routingContext) throws Exception {
        getVertx().runOnContext(v -> routingContext.response()
                .end("Test runOnSameEventLoop succeeded"));
    }
}
