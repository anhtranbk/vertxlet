package examples;

import com.github.vertxlet.core.Inject;
import com.github.vertxlet.core.Context;
import com.github.vertxlet.core.spring.Controller;
import com.github.vertxlet.core.spring.RequestMapping;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

@Controller
public class InjectDemoController {

    @Inject
    private Context ctx;

    @RequestMapping(path = "/welcome", method = HttpMethod.GET)
    public void printAppInfo(RoutingContext rc) {
        rc.response().end("App name: " + ctx.config().getString("example.appName"));
    }
}
