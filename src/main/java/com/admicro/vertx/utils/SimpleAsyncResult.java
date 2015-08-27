package com.admicro.vertx.utils;

import io.vertx.core.AsyncResult;

public class SimpleAsyncResult<T> implements AsyncResult {

    private T result;
    private Throwable cause;
    private boolean succeeded;

    public SimpleAsyncResult(T result) {

    }

    public SimpleAsyncResult(Throwable cause) {

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
