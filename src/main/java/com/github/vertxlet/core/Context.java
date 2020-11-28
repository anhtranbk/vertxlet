package com.github.vertxlet.core;

import com.github.vertxlet.util.Config;
import io.vertx.core.Vertx;

public interface Context {

    Vertx vertx();

    Config config();
}
