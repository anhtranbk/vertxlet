package com.github.vertxlet.core;

import com.github.vertxlet.util.Config;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;

public class Server {

    public static final String DB_KEY = "vertxlet.db-key";

    private final VertxOptions vertxOptions;
    private final DeploymentOptions deploymentOptions;
    private final Config conf;

    Server(VertxOptions vertxOptions, DeploymentOptions deploymentOptions, Config conf) {
        this.vertxOptions = vertxOptions;
        this.deploymentOptions = deploymentOptions;
        this.conf = conf;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Vertx start() {
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME,
                SLF4JLogDelegateFactory.class.getName());
        LoggerFactory.initialise();

        Vertx vertx = Vertx.vertx(this.vertxOptions);
        Verticle server = new ServerVerticle(conf);

        vertx.deployVerticle(server, deploymentOptions, ar -> {
            if (ar.failed()) {
                vertx.close(v -> {
                    System.err.println("Error when deploy verticle, vertx closed");
                    ar.cause().printStackTrace();
                });
            }
        });
        return vertx;
    }

    public static ServerBuilder builder() {
        return new ServerBuilder();
    }
}
