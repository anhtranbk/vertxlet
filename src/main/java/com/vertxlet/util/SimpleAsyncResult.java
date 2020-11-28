package com.vertxlet.util;

import io.vertx.core.AsyncResult;

public class SimpleAsyncResult<T> implements AsyncResult<T> {

    private final T result;
    private final Throwable cause;
    private final boolean succeeded;

    public SimpleAsyncResult(T result) {
        this(result, null, true);
    }

    public SimpleAsyncResult(Throwable cause) {
        this(null, cause, false);
    }

    public SimpleAsyncResult(T result, Throwable cause, boolean succeeded) {
        this.result = result;
        this.cause = cause;
        this.succeeded = succeeded;
    }

    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public boolean succeeded() {
        return succeeded;
    }

    @Override
    public boolean failed() {
        return !succeeded;
    }
}
