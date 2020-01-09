package examples;

import com.admicro.vertxlet.core.AbstractVertxlet;
import com.admicro.vertxlet.core.db.Jdbc;
import com.admicro.vertxlet.core.VertxletMapping;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

@Jdbc
@VertxletMapping(url = {"/jdbc/query/:id"})
public class JdbcExamples extends AbstractVertxlet {

    @Override
    protected void doGet(RoutingContext rc) throws Exception {
        SQLConnection con = getSqlConnection(rc);
        int id = Integer.parseInt(rc.request().getParam("id"));

        final String query = "SELECT id, value FROM random_value WHERE id = " + id;
        con.query(query, select -> {
            if (select.failed()) {
                rc.fail(select.cause());
                return;
            }
            try {
                ResultSet rs = select.result();
                int val = rs.getRows().get(0).getInteger("value");
                rc.response().end("val=" + val + "\r\n");
            } catch (IndexOutOfBoundsException e) {
                rc.fail(e);
            }
        });
    }
}
