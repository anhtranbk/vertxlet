package com.github.vertxlet.core;

import com.github.vertxlet.util.Config;

public class ServerOptions {

    public static final int DEFAULT_PORT = 3310;
    public static final String DEFAULT_ADDRESS = "127.0.0.1";
    public static final int DEFAULT_TIMEOUT = 2000;

    private final int port;
    private final String address;
    private final int timeout;
    private final boolean enableLoggerHandler;
    private final boolean enableResponseTimeHandler;

    public ServerOptions() {
        this(new Config());
    }

    public ServerOptions(Config conf) {
        port = conf.getInt("http.server.port", DEFAULT_PORT);
        address = conf.getString("http.server.address", DEFAULT_ADDRESS);
        timeout = conf.getInt("http.timeout", DEFAULT_TIMEOUT);
        enableLoggerHandler = conf.getBool("http.enableLoggerHandler", true);
        enableResponseTimeHandler = conf.getBool("http.enableResponseTimeHandler", false);
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

    public boolean isEnableResponseTimeHandler() {
        return enableResponseTimeHandler;
    }
}
