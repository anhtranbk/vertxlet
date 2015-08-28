package com.admicro.vertx.core;

import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

/**
 * @author anhtn
 */
public interface Vertxlet {

    /**
     * Initialise the vertxlet<p>
     * This is called by verticle when create new vertxlet instance. Don't call it yourself.
     * @param vertx the deploying Vert.x instance
     * @param verticle the verticle instance that create vertxlet
     */
    void setContext(Vertx vertx, Verticle verticle);

    /**
     * This is called by verticle when create new vertxlet instance. Don't call it yourself.<p>
     * Like traditional JavaEE Servlet, it is called once.
     * @param future a future which should be called when vertxlet init is complete
     * @throws Exception
     */
    default void init(Future<Void> future) throws Exception {
        future.complete();
    }

    /**
     * This is called by verticle when verticle is un-deployed. Don't call it yourself.<p>
     * Like traditional JavaEE Servlet, it is called once.
     * @param future a future which should be called when vertxlet destroy is complete
     * @throws Exception
     */
    default void destroy(Future<Void> future) throws Exception {
        future.complete();
    }

    /**
     * This is called by verticle when verticle need to handle a request. Don't call it yourself.<p>
     * @param routingContext the context of the Route
     */
    void handle(RoutingContext routingContext);

    /**
     * Get the Vert.x instance
     * @return the Vert.x instance
     */
    Vertx getVertx();

    /**
     * Get the verticle instance
     * @return the verticle instance
     */
    Verticle getVerticle();
}
