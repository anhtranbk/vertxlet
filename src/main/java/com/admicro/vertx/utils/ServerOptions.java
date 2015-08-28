package com.admicro.vertx.utils;

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

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
