package com.vertxlet.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("JavaReflectionInvocation")
public class ClassLoaders {

    public static Object loadClass(Class<?> clazz, Object... params) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<?> constructor = clazz.getConstructor(); // get default constructor
        return constructor.newInstance(params);
    }
}
