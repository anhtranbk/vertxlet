package examples;

import com.github.vertxlet.core.AbstractVertxlet;
import com.github.vertxlet.core.VertxletMapping;
import io.vertx.ext.web.RoutingContext;

@VertxletMapping(url = {"/delay/:delay"})
public class DelayVertxlet extends AbstractVertxlet {

    @Override
    protected void doGet(RoutingContext rc) throws Exception {
        long delay = Long.parseLong(rc.request().getParam("delay"));
        rc.vertx().setTimer(delay, ar -> rc.response()
                .end("=>" + delay + "\r\n"));
    }

    @Override
    protected void doPost(RoutingContext rc) throws Exception {
        rc.vertx().runOnContext(v -> rc.response()
                .end("Test runOnSameEventLoop succeeded"));
    }
}
