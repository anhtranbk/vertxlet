package com.admicro.vertxlet.core.impl;

import io.vertx.core.json.JsonObject;

public class ServerOptions {

    public static final int DEFAULT_PORT = 8888;
    public static final String DEFAULT_ADDRESS = "0.0.0.0";

    private int port = DEFAULT_PORT;
    private String address = DEFAULT_ADDRESS;

    public ServerOptions() {
    }

    public ServerOptions(JsonObject json) {
        port = json.getInteger("port", DEFAULT_PORT);
        address = json.getString("address", DEFAULT_ADDRESS);
    }

    public String getAddress() {
        return address;
    }

    public ServerOptions setAddress(String address) {
        this.address = address;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ServerOptions setPort(int port) {
        this.port = port;
        return this;
    }
}
