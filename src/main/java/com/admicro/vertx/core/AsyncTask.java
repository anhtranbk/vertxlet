package com.admicro.vertx.core;

public interface AsyncTask<T> {

    T run() throws Exception;
}
