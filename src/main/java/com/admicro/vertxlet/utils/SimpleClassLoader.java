package com.admicro.vertxlet.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SimpleClassLoader {

    public static Object loadClass(Class<?> clazz, Object... params) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<?> constructor = clazz.getConstructor(); // get default constructor
        return constructor.newInstance(params);
    }
}
