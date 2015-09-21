package com.admicro.vertxlet.core;

import com.admicro.vertxlet.core.impl.HttpServerVerticle;
import com.admicro.vertxlet.core.impl.ServerOptions;
import com.admicro.vertxlet.util.FileUtils;
import com.admicro.vertxlet.util.XmlConverter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface HttpServer {

    public static void startNew() throws VertxletException {
        HttpServer.startNew(HttpContext.defaultContext());
    }

    public static void startNew(HttpContext serverContext) throws VertxletException {
        try {
            Vertx vertx = Vertx.vertx();
            String xmlContent = FileUtils.readAll(vertx, serverContext.configurationPath())
                    .replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ", "");
            JsonObject config = XmlConverter.toJson(xmlContent, HttpContext.ROOT_TAG).getJsonObject("server");

            // apply vertxlet options
            if (config.containsKey("vertx_options")) {
                vertx = Vertx.vertx(new VertxOptions(config.getJsonObject("vertx_options")));
            }

            // apply database options
            if (config.containsKey("database_options")) {
                JsonObject dbConfig = config.getJsonObject("database_options");
                vertx.sharedData().getLocalMap(HttpServerVerticle.DEFAULT_SHARE_LOCAL_MAP)
                        .put("db_options", dbConfig);
            }

            // apply server options
            ServerOptions options = (config.containsKey("server_options"))
                    ? new ServerOptions(config.getJsonObject("server_options")) : new ServerOptions();
            HttpServerVerticle server = new HttpServerVerticle(options);

            // apply deployment options
            DeploymentOptions deploymentOptions = (config.containsKey("deployment_options"))
                    ? new DeploymentOptions(config.getJsonObject("deployment_options")) : null;
            if (deploymentOptions != null) {
                vertx.deployVerticle(server, deploymentOptions);
            } else {
                vertx.deployVerticle(server);
            }
        } catch (IOException | ParserConfigurationException | SAXException | NullPointerException e) {
            throw new VertxletException(e);
        }
    }
}
