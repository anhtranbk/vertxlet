package com.github.vertxlet.core.impl;

import com.github.vertxlet.core.Context;
import com.github.vertxlet.util.Config;
import io.vertx.core.Vertx;

public class VertxletContextImpl implements Context {

    private final Vertx vertx;
    private final Config conf;

    public VertxletContextImpl(Vertx vertx, Config conf) {
        this.vertx = vertx;
        this.conf = conf;
    }

    @Override
    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public Config config() {
        return this.conf;
    }
}
