package com.admicro.vertxlet.util;

import io.vertx.core.Future;

public interface RunnableFuture<T> {

    void run(Future<T> future);
}
