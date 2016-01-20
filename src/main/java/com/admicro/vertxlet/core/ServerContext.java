package com.admicro.vertxlet.core;

public interface ServerContext {

    String ROOT_TAG = "server";

    String configurationPath();

    static ServerContext defaultContext() {
        return () -> "server.xml";
    }
}
