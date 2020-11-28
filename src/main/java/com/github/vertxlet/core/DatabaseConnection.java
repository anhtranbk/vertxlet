package com.github.vertxlet.core;

import io.vertx.core.Future;

public interface DatabaseConnection<T> {

    Future<T> connect();

    Future<Void> close();
}
