package com.admicro.vertx.core;

public interface HttpContext {

    static final String ROOT_TAG = "server";

    String configurationPath();

    static HttpContext defaultContext() {
        return () -> "server.xml";
    }
}
