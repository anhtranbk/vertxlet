package com.admicro.vertxlet.core;

import com.admicro.vertxlet.util.FileUtils;
import com.admicro.vertxlet.util.XmlConverter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface HttpServer {

    static final String LOG4J_DELEGATE_FACTORY_CLASS = "io.vertx.core.logging.Log4jLogDelegateFactory";
    static final String SLF4J_DELEGATE_FACTORY_CLASS = "io.vertx.core.logging.SLF4JLogDelegateFactory";
    static final String JUL_DELEGATE_FACTORY_CLASS = "io.vertx.core.logging.JULLogDelegateFactory";

    static final String LOGGING_PROPERTY_KEY = "vertx.logger-delegate-factory-class-name";

    static final String LOG4J = "log4j";
    static final String SLF4J = "slf4j";

    public static void startNew() throws VertxletException {
        HttpServer.startNew(HttpContext.defaultContext());
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static void startNew(HttpContext serverContext) throws VertxletException {
        try {
            final ClassLoader cl = HttpServer.class.getClassLoader();
            String xmlContent = FileUtils.readAll(cl, serverContext.configurationPath())
                    .replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ", "");

            JsonObject config = XmlConverter.toJson(xmlContent, HttpContext.ROOT_TAG)
                    .getJsonObject("server");

            // apply system properties before create new Vert.x instance
            JsonObject properties;
            if ((properties = config.getJsonObject("properties")) != null) {
                String delegate = JUL_DELEGATE_FACTORY_CLASS;

                String logger = properties.getString("logger");
                if (LOG4J.equalsIgnoreCase(logger)) {
                    delegate = LOG4J_DELEGATE_FACTORY_CLASS;
                } else if (SLF4J.equalsIgnoreCase(logger)) {
                    delegate = SLF4J_DELEGATE_FACTORY_CLASS;
                }

                System.setProperty(LOGGING_PROPERTY_KEY, delegate);
            }

            Vertx vertx;

            // apply vertxlet options
            if (config.containsKey("vertx_options")) {
                vertx = Vertx.vertx(new VertxOptions(config.getJsonObject("vertx_options")));
            } else {
                vertx = Vertx.vertx();
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
            Verticle server = new HttpServerVerticle(options);

            // apply deployment options
            DeploymentOptions deploymentOptions = (config.containsKey("deployment_options"))
                    ? new DeploymentOptions(config.getJsonObject("deployment_options"))
                    : new DeploymentOptions();
            vertx.deployVerticle(server, deploymentOptions, ar -> {
                if (ar.failed()) {
                    vertx.close(v -> {
                        System.err.println("Error when deploy verticle, vertx closed");
                        ar.cause().printStackTrace();
                    });
                }
            });
        } catch (IOException | ParserConfigurationException | SAXException | NullPointerException e) {
            throw new VertxletException(e);
        }
    }
}
