package com.admicro.vertx.core;

public interface HttpServerContext {

    static final String ROOT_TAG = "server";

    String configurationPath();

    static HttpServerContext defaultContext() {
        return new HttpServerContext() {
            @Override
            public String configurationPath() {
                return "server.xml";
            }
        };
    }
}
