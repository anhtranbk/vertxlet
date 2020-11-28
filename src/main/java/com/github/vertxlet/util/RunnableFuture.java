package com.github.vertxlet.util;

import io.vertx.core.Future;

public interface RunnableFuture<T> {

    void run(Future<T> future);
}
