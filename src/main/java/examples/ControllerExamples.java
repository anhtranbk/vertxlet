package examples;

import com.admicro.vertxlet.core.mvc.Controller;
import com.admicro.vertxlet.core.mvc.RequestMapping;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

@Controller("/user")
public class ControllerExamples {

    @RequestMapping(path = {"/login/:user", "/authenticate/:user"}, method = HttpMethod.GET)
    public void login(RoutingContext rc) {
        rc.response().end("Login success with user: " + rc.request().getParam("user")
                + " at path: " + rc.request().path());
    }

    @RequestMapping(path = "/register", method = HttpMethod.GET)
    public void register(RoutingContext rc) {
        rc.response().end("Register success");
    }
}
