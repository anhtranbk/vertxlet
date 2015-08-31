package com.admicro.vertx.utils;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XmlConverterTest {

    @Test
    public void test_toJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("<server>\n");
        builder.append("<server_options>\n" +
                "<address>0.0.0.0</address>\n" +
                "<port>8888</port>\n" +
                "</server_options>");
        builder.append("<database>\n" +
                "<driver_class>com.mysql.jdbc.Driver</driver_class>\n" +
                "<url>jdbc:mysql://localhost/server_load</url>\n" +
                "<user>root</user>\n" +
                "<password>root</password>\n" +
                "<max_pool_size>30</max_pool_size>\n" +
                "</database>\n");
        builder.append("<deployment_options></deployment_options>\n");
        builder.append("<vertx_options></vertx_options>\n");
        builder.append("</server>");

        JsonObject server = null;
        try {
            server = XmlConverter.toJson(builder.toString(), "server").getJsonObject("server");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        JsonObject serverOptions = server.getJsonObject("server_options");
        JsonObject database = server.getJsonObject("database");
        JsonObject deploymentOptions = server.getJsonObject("deployment_options");
        JsonObject vertxOptions = server.getJsonObject("vertx_options");

        assertTrue(serverOptions != null);
        assertTrue(database != null);
        assertTrue(deploymentOptions != null);
        assertTrue(vertxOptions != null);

        assertEquals(serverOptions.getString("address"), "0.0.0.0");
        assertEquals(serverOptions.getString("port"), "8888");

        assertEquals(database.getString("driver_class"), "com.mysql.jdbc.Driver");
        assertEquals(database.getString("url"), "jdbc:mysql://localhost/server_load");
        assertEquals(database.getString("user"), "root");
        assertEquals(database.getString("password"), "root");
        assertEquals(database.getString("max_pool_size"), "30");
    }
}
