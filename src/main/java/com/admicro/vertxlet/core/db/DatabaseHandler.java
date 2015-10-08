package com.admicro.vertxlet.core.db;

import com.admicro.vertxlet.core.db.impl.DatabaseHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.lang.annotation.Annotation;

public interface DatabaseHandler extends Handler<RoutingContext> {

    static DatabaseHandler create(Class<? extends Annotation> clazz) {
        return new DatabaseHandlerImpl(clazz);
    }
}
