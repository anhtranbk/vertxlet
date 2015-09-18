package examples;

import com.admicro.vertxlet.core.HttpVertxlet;
import com.admicro.vertxlet.core.db.Jdbc;
import com.admicro.vertxlet.core.RequestMapping;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

@Jdbc
@RequestMapping(url = {"/jdbc/query/:id"})
public class JdbcExamples extends HttpVertxlet {

    @Override
    protected void doGet(RoutingContext routingContext) throws Exception {
        SQLConnection con;
        int id;
        try {
            con = getSqlConnection(routingContext);
            id = Integer.parseInt(routingContext.request().getParam("id"));

            final String query = "SELECT id, value FROM random_value WHERE id = " + id;
            con.query(query, res -> {
                if (res.succeeded()) {
                    try {
                        ResultSet rs = res.result();
                        int val = rs.getRows().get(0).getInteger("value");
                        routingContext.response().end("val=" + val + "\r\n");
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        routingContext.response().setStatusCode(500).end(getPrintableStackTrace(e));
                    }
                } else {
                    routingContext.response().setStatusCode(500).end(getPrintableStackTrace(res.cause()));
                }
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
