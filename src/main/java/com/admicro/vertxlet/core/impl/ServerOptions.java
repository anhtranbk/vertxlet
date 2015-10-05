package com.admicro.vertxlet.core.impl;

import io.vertx.core.json.JsonObject;

public class ServerOptions {

    public static final int DEFAULT_PORT = 8888;
    public static final String DEFAULT_ADDRESS = "0.0.0.0";
    public static final int DEFAULT_TIMEOUT = 2000;

    private int port = DEFAULT_PORT;
    private String address = DEFAULT_ADDRESS;
    private int timeout = DEFAULT_TIMEOUT;
    private boolean enableLoggerHandler = false;
    private boolean enableResponseTimeHandler = false;

    public ServerOptions() {
    }

    public ServerOptions(JsonObject json) {
        port = json.getInteger("port", DEFAULT_PORT);
        address = json.getString("address", DEFAULT_ADDRESS);
        timeout = json.getInteger("timeout", DEFAULT_TIMEOUT);
        enableLoggerHandler = json.getInteger("enableLoggerHandler", 0) != 0;
        enableResponseTimeHandler = json.getInteger("enableResponseTimeHandler", 0) != 0;
    }

    public String address() {
        return address;
    }

    public int port() {
        return port;
    }

    public int timeout() {
        return timeout;
    }

    public boolean isEnableLoggerHandler() {
        return enableLoggerHandler;
    }

    public boolean isEnableReponseTimeHandler() {
        return enableResponseTimeHandler;
    }
}
