package com.github.vertxlet.core;

import com.github.vertxlet.util.Config;
import com.github.vertxlet.util.FileUtils;
import com.github.vertxlet.util.XmlConverter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class ServerBuilder {

    private VertxOptions vertxOptions = new VertxOptions();
    private DeploymentOptions deploymentOptions = new DeploymentOptions();
    private Config conf = new Config();

    public ServerBuilder vertxOptions(VertxOptions options) {
        this.vertxOptions = options;
        return this;
    }

    public ServerBuilder vertxOptions(JsonObject json) {
        this.vertxOptions = new VertxOptions(json);
        return this;
    }

    public ServerBuilder deploymentOptions(DeploymentOptions options) {
        this.deploymentOptions = options;
        return this;
    }

    public ServerBuilder deploymentOptions(JsonObject json) {
        this.deploymentOptions = new DeploymentOptions(json);
        return this;
    }

    public ServerBuilder applicationConfig(Config conf) {
        this.conf = conf;
        return this;
    }

    public ServerBuilder loadSystemConfig(String path) {
        try {
            String xml = FileUtils.readAll(path)
                    .replaceAll("\t", "")
                    .replaceAll("\n", "")
                    .replaceAll(" ", "");
            JsonObject config = XmlConverter.toJson(xml, "server").getJsonObject("server");

            this.vertxOptions =  config.containsKey("vertx.options")
                    ? new VertxOptions(config.getJsonObject("vertx.options"))
                    : new VertxOptions();

            // apply deployment options
            this.deploymentOptions = (config.containsKey("deployment.options"))
                    ? new DeploymentOptions(config.getJsonObject("deployment.options"))
                    : new DeploymentOptions();
        } catch (Exception e) {
            throw new VertxletException(e);
        }
        return this;
    }

    public ServerBuilder loadApplicationConfig(String path) {
        this.conf = new Config(path);
        return this;
    }

    public Server build() {
        return new Server(vertxOptions, deploymentOptions, conf);
    }
}
