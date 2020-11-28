package com.github.vertxlet.core;

public class VertxletException extends RuntimeException {

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
