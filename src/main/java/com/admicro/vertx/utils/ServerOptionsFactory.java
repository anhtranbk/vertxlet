package com.admicro.vertx.utils;

import com.sun.istack.internal.NotNull;
import io.vertx.core.Vertx;

import java.io.IOException;

public class ServerOptionsFactory {

    static final String DEFAULT_PATH = "server.xml";

    public static ServerOptions defaultServerOption(Vertx vertx) {
        ServerOptions options;
        try {
            options = fromXml(vertx, DEFAULT_PATH);
            return options;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ServerOptions();
    }

    public static ServerOptions fromXml(@NotNull Vertx vertx, String path)
            throws IOException, NullPointerException {

        String xml = FileUtils.readAll(vertx, path);
        return new ServerOptions(XmlParser.toJson(xml));
    }
}
