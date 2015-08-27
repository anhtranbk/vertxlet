package com.admicro.vertx.core;

public class VertxletException extends Exception {

    public VertxletException(String message) {
        super(message);
    }

    public VertxletException(String message, Throwable cause) {
        super(message, cause);
    }

    public VertxletException(Throwable cause) {
        super(cause);
    }

    public VertxletException() {

    }
}
