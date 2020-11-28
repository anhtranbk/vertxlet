package com.github.vertxlet.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("JavaReflectionInvocation")
public class ReflectionUtils {

    public static Object loadClass(Class<?> clazz, Object... params) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<?> constructor = clazz.getConstructor(); // get default constructor
        return constructor.newInstance(params);
    }

    public static void inject(Object obj, Object value, Class<? extends Annotation> annotation)
            throws IllegalAccessException {

        for (Field field : obj.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(annotation)) continue;
            if (!field.getType().isAssignableFrom(value.getClass())) continue;

            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(obj, value);

            // reset accessible to original value
            field.setAccessible(isAccessible);
        }
    }
}
