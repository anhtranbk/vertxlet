package examples;

import com.admicro.vertx.core.HttpVertxlet;
import com.admicro.vertx.core.VertxletRequestMapping;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.RoutingContext;

import java.util.Random;

@VertxletRequestMapping(url = {"/jdbc"}, usingDatabase = true)
public class Jdbc2Examples extends HttpVertxlet {

    @Override
    protected void doGet(RoutingContext routingContext) throws Exception {
        String st = "INSERT INTO random_value (value) VALUES (?)";
        JsonArray params = new JsonArray().add(new Random().nextInt());
        getSqlConnection(routingContext).updateWithParams(st, params, insert -> {
            if (insert.failed()) {
                insert.cause().printStackTrace();
                return;
            }

            StringBuilder sb = new StringBuilder();
            UpdateResult ur = insert.result();

            sb.append("Number rows updated: ").append(ur.getUpdated()).append("\r\n");
            sb.append("Last id inserted: ").append(ur.getKeys().getInteger(0)).append("\r\n");

            routingContext.response().end(sb.toString());
        });
    }
}
