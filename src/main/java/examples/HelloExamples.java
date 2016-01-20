package examples;

import com.admicro.vertxlet.core.AbstractVertxlet;
import com.admicro.vertxlet.core.VertxletMapping;
import io.vertx.ext.web.RoutingContext;

@VertxletMapping(url = {"/id/:id"})
public class HelloExamples extends AbstractVertxlet {

    @Override
    protected void doGet(RoutingContext routingContext) throws Exception {
        String id = routingContext.request().getParam("id");
        if (id != null) {
            routingContext.response().end("Hello, id=" + id);
        } else {
            routingContext.response().end("Hello, current path: " + routingContext.request().path());
        }
    }

    @Override
    protected void doPost(RoutingContext routingContext) throws Exception {
        super.doPost(routingContext);
    }
}
