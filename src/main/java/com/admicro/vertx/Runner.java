package com.admicro.vertx;

import com.admicro.vertx.core.HttpServerVerticle;
import io.vertx.core.Vertx;

public class Runner {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new HttpServerVerticle());
    }
}
