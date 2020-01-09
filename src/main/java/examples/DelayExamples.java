package examples;

import com.admicro.vertxlet.core.AbstractVertxlet;
import com.admicro.vertxlet.core.VertxletMapping;
import io.vertx.ext.web.RoutingContext;

@VertxletMapping(url = {"/delay/:delay"})
public class DelayExamples extends AbstractVertxlet {

    @Override
    protected void doGet(RoutingContext rc) throws Exception {
        long delay = Long.parseLong(rc.request().getParam("delay"));
        getVertx().setTimer(delay, ar -> rc.response()
                .end("=>" + delay + "\r\n"));
    }

    @Override
    protected void doPost(RoutingContext rc) throws Exception {
        getVertx().runOnContext(v -> rc.response()
                .end("Test runOnSameEventLoop succeeded"));
    }
}
