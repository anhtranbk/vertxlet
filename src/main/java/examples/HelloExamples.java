package examples;

import com.admicro.vertxlet.core.AbstractVertxlet;
import com.admicro.vertxlet.core.VertxletMapping;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@VertxletMapping(url = {"/id/:id", "/id"})
public class HelloExamples extends AbstractVertxlet {

    @Override
    protected void doGet(RoutingContext rc) throws Exception {
        String id = rc.request().getParam("id");
        if (id != null) {
            JsonObject json = new JsonObject()
                    .put("id", id)
                    .put("ts", System.currentTimeMillis());
            rc.response()
                    .putHeader("Content-Type", "application/json")
                    .end(json.toString());
        } else {
            rc.response().end("Hello, current path: " + rc.request().path());
        }
    }

    @Override
    protected void doPost(RoutingContext routingContext) throws Exception {
        super.doPost(routingContext);
    }
}
