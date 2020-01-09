package examples;

import com.admicro.vertxlet.core.VertxletMapping;
import com.admicro.vertxlet.core.AbstractVertxlet;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

@VertxletMapping(url = {"/server-load/delay/:delay"})
public class Delay2Examples extends AbstractVertxlet {

    private static final Logger logger = LoggerFactory.getLogger(Delay2Examples.class);

    @Override
    protected void doGet(RoutingContext rc) throws Exception {
        final long start = System.currentTimeMillis();
        getVertx().executeBlocking(fut -> {
            long delay = Long.parseLong(rc.request().getParam("delay"));
            try {
                Thread.sleep(delay);
                fut.complete();
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                fut.fail(e);
            }
        }, false, ar -> rc.response().end("=>" + (System.currentTimeMillis() - start) + "\r\n"));
    }
}
