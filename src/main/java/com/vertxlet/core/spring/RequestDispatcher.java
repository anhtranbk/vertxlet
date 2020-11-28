package com.vertxlet.core.spring;

import com.vertxlet.core.VertxletException;
import com.vertxlet.util.ClassLoaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RequestDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);

    public static void init(Router router) throws VertxletException{
        final Reflections reflections = new Reflections("");

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Controller.class)) {
            String controllerUrl = clazz.getAnnotation(Controller.class).value();
            if (!controllerUrl.startsWith("/")) {
                throw new VertxletException("Path must be start with /");
            }

            Object controller;
            try {
                controller = ClassLoaders.loadClass(clazz);
            } catch (Exception e) {
                logger.error("Load class failed " + clazz.getName(), e);
                continue;
            }

            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(RequestMapping.class)) continue;

                RequestMapping rm = method.getAnnotation(RequestMapping.class);
                String[] paths = rm.path();
                HttpMethod[] httpMethods = rm.method();

                for (String path : paths) {
                    if (!path.startsWith("/")) {
                        throw new VertxletException("Path must be start with /");
                    }
                    for (HttpMethod httpMethod : httpMethods) {
                        if (controllerUrl.equals("/")) controllerUrl = "";
                        router.route(httpMethod, controllerUrl + path).handler(rc -> {
                            try {
                                method.invoke(controller, rc);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                logger.error(null, e);
                            }
                        });
                        logger.info(String.format("Mapping %s:%s with %s.%s()",
                                httpMethod.toString(), controllerUrl + path,
                                clazz.getSimpleName(), method.getName()));
                    }
                }
            }
        }
    }
}
