package examples;

import com.admicro.vertxlet.core.mvc.Controller;
import com.admicro.vertxlet.core.mvc.RequestMapping;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

@Controller("/user")
public class ControllerExamples {

    private static final Logger logger = LoggerFactory.getLogger(ControllerExamples.class);

    // Controller object created only once per Vertx context
    private final Map<String, Long> users = new HashMap<>();

    @RequestMapping(path = {"/login/:user", "/authenticate/:user"}, method = HttpMethod.GET)
    public void login(RoutingContext rc) {
        String user = rc.request().getParam("user");
        Long ts = users.get(user);
        if (ts != null) {
            // demo return json response
            rc.response()
                    .putHeader("Content-type", "application/json")
                    .end(new JsonObject().put("user", user).put("created_at", ts).toString());
        } else {
            rc.response().setStatusCode(404).end("Login failed. User not found");
        }
    }

    @RequestMapping(path = "/register/:user", method = HttpMethod.GET)
    public void register(RoutingContext rc) {
        String user = rc.request().getParam("user");
        if (user == null || user.isEmpty()) {
            rc.response().setStatusCode(400).end("Invalid input");
            return;
        }
        if (!users.containsKey(user)) {
            users.put(user, System.currentTimeMillis());
            rc.response().end("Register success");
        } else {
            rc.response().setStatusCode(409).end("User exists");
        }
    }
}
