package com.admicro.vertxlet.core.db;

public class DbAdaptorFactory {

    public static IDbAdaptor iDbAdaptor(String type) {
        if ("jdbc".equalsIgnoreCase(type)) {
            return new JdbcAdaptor();
        } else if ("redis".equalsIgnoreCase(type)) {
            return new RedisAdaptor();
        } else return null;
    }
}
