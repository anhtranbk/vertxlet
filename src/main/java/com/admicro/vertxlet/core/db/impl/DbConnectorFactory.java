package com.admicro.vertxlet.core.db.impl;

import com.admicro.vertxlet.core.db.IDbConnector;

public interface DbConnectorFactory {

    public static IDbConnector iDbAdaptor(String type) {
        if ("jdbc".equalsIgnoreCase(type)) {
            return new JdbcConnector();
        } else if ("redis".equalsIgnoreCase(type)) {
            return new RedisConnector();
        } else return null;
    }
}
