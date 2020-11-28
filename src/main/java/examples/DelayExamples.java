package examples;

import com.vertxlet.core.AbstractVertxlet;
import com.vertxlet.core.VertxletMapping;
import io.vertx.ext.web.RoutingContext;

@VertxletMapping(url = {"/delay/:delay"})
public class DelayExamples extends AbstractVertxlet {

    @Override
    protected void doGet(RoutingContext rc) throws Exception {
        long delay = Long.parseLong(rc.request().getParam("delay"));
        vertx().setTimer(delay, ar -> rc.response()
                .end("=>" + delay + "\r\n"));
    }

    @Override
    protected void doPost(RoutingContext rc) throws Exception {
        vertx().runOnContext(v -> rc.response()
                .end("Test runOnSameEventLoop succeeded"));
    }
}
