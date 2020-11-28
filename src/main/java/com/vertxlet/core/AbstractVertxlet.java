package com.vertxlet.core;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractVertxlet implements Vertxlet {

    private Vertx vertx;

    @Override
    public void vertx(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public <T> void init(Promise<T> promise) {
        try {
            init();
            promise.complete();
        } catch (Exception e) {
            promise.fail(e);
        }
    }

    @Override
    public <T> void destroy(Promise<T> promise) {
        try {
            destroy();
            promise.complete();
        } catch (Exception e) {
            promise.fail(e);
        }
    }

    @Override
    public void handle(RoutingContext rc) {
        try {
            switch (rc.request().method()) {
                case GET:
                    doGet(rc);
                    break;
                case POST:
                    doPost(rc);
                    break;
                case PUT:
                    doPut(rc);
                    break;
                case HEAD:
                    doHead(rc);
                    break;
                case DELETE:
                    doDelete(rc);
                    break;
                default:
                    throw new IllegalArgumentException("HTTP method: "
                            + rc.request().method() + " is not supported");
            }
        } catch (Exception e) {
            rc.fail(e);
        }
    }

    @Override
    public final Vertx vertx() {
        return vertx;
    }

    protected void init() throws Exception {
    }

    protected void destroy() throws Exception {
    }

    protected void doGet(RoutingContext rc) throws Exception {
        rc.response().end();
    }

    protected void doPost(RoutingContext rc) throws Exception {
        rc.response().end();
    }

    protected void doPut(RoutingContext rc) throws Exception {
        rc.response().end();
    }

    protected void doHead(RoutingContext rc) throws Exception {
        rc.response().end();
    }

    protected void doDelete(RoutingContext rc) throws Exception {
        rc.response().end();
    }
}