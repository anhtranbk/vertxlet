package examples;

import com.admicro.vertxlet.core.HttpVertxlet;
import com.admicro.vertxlet.core.db.Jdbc;
import com.admicro.vertxlet.core.RequestMapping;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.RoutingContext;

@Jdbc
@RequestMapping(url = {"/jdbc/insert/:id"})
public class Jdbc2Examples extends HttpVertxlet {

    @Override
    protected void doGet(RoutingContext routingContext) throws Exception {
        SQLConnection con;
        int id;
        try {
            con = getSqlConnection(routingContext);
            id = Integer.parseInt(routingContext.request().getParam("id"));

            String st = "INSERT INTO random_value (value) VALUES (?)";
            JsonArray params = new JsonArray().add(id);
            con.updateWithParams(st, params, insert -> {
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
        } catch (UnsupportedOperationException | NumberFormatException e) {
            e.printStackTrace();
            routingContext.response().end(getPrintableStackTrace(e));
        }
    }

    private String getPrintableStackTrace(Throwable throwable) {
        StringBuilder builder = new StringBuilder(throwable.getMessage() + "\n");
        for (StackTraceElement ste : throwable.getStackTrace()) {
            builder.append(ste.toString()).append("\n");
        }
        return builder.toString();
    }
}
