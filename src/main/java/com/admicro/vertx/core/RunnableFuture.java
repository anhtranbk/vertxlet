package com.admicro.vertx.core;

import io.vertx.core.Future;

public interface RunnableFuture<T> {

    void run(Future<T> future);
}
