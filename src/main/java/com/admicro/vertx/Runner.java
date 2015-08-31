package com.admicro.vertx;

import com.admicro.vertx.core.HttpServer;
import com.admicro.vertx.core.HttpServerContext;
import com.admicro.vertx.core.VertxletException;

public class Runner {

    public static void main(String[] args) {
        try {
            HttpServer.startNew(HttpServerContext.defaultContext());
        } catch (VertxletException e) {
            e.printStackTrace();
        }
    }
}
