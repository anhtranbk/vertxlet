package examples;

import com.admicro.vertxlet.core.HttpVertxlet;
import com.admicro.vertxlet.core.RequestMapping;
import io.vertx.ext.web.RoutingContext;

@RequestMapping(url = {"/delay/:delay"})
public class DelayExamples extends HttpVertxlet {

    @Override
    protected void doGet(RoutingContext routingContext) throws Exception {
        System.out.println("Received request from: " + routingContext.request().path());
        runDelay(() -> routingContext.response().end("Task delay executed"),
                Long.parseLong(routingContext.request().getParam("delay")));
    }

    @Override
    protected void doPost(RoutingContext routingContext) throws Exception {
        runOnSameEventLoop(() -> routingContext.response().end("Test runOnSameEventLoop succeeded"));
    }
}
