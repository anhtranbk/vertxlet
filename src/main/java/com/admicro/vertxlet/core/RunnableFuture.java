package com.admicro.vertxlet.core;

import io.vertx.core.Future;

public interface RunnableFuture<T> {

    void run(Future<T> future);
}
