package com.admicro.vertx.examples;

import com.admicro.vertx.core.HttpVertxlet;
import com.admicro.vertx.core.VertxServlet;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;

@VertxServlet(url = "/id/:id")
public class TestJdbcVertxlet extends HttpVertxlet {

    @Override
    protected void doGet(RoutingContext routingContext) {
        final SQLConnection con = getSqlConnection(routingContext);
        final int id = Integer.parseInt(routingContext.request().getParam("id"));

        final String query = "SELECT value FROM random_value WHERE id = " + id;
        con.query(query, res -> {
            if (res.succeeded()) {
                try {
                    ResultSet rs = res.result();
                    int val = rs.getRows().get(0).getInteger("value");
                    routingContext.response().end("val=" + val);
                } catch (IndexOutOfBoundsException e) {
                    routingContext.response().setStatusCode(500).end(e.getMessage());
                }
            } else {
                routingContext.response().setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }
}
