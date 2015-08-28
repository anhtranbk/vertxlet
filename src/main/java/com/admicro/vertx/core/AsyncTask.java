package com.admicro.vertx.core;

/**
 * @author anhtn
 * @param <T> the type of the result
 */
public interface AsyncTask<T> {

    T run() throws Exception;
}
