package examples;

import com.admicro.vertxlet.core.AbstractVertxlet;
import com.admicro.vertxlet.core.db.Jdbc;
import com.admicro.vertxlet.core.VertxletMapping;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.RoutingContext;

@Jdbc
@VertxletMapping(url = {"/jdbc/insert/:value"})
public class Jdbc2Examples extends AbstractVertxlet {

    @Override
    protected void doGet(RoutingContext rc) throws Exception {
        SQLConnection con = getSqlConnection(rc);
        int value = Integer.parseInt(rc.request().getParam("value"));

        String st = "INSERT INTO random_value (value) VALUES (?)";
        JsonArray params = new JsonArray().add(value);
        con.updateWithParams(st, params, insert -> {
            if (insert.failed()) {
                rc.fail(insert.cause());
                return;
            }

            StringBuilder sb = new StringBuilder();
            UpdateResult ur = insert.result();

            sb.append("Number rows updated: ").append(ur.getUpdated()).append("\r\n");
            sb.append("Last id inserted: ").append(ur.getKeys().getInteger(0)).append("\r\n");

            rc.response().end(sb.toString());
        });
    }
}
