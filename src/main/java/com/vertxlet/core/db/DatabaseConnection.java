package com.vertxlet.core.db;

import io.vertx.core.Future;

public interface DatabaseConnection<T> {

    Future<T> connect();

    Future<Void> close();
}
