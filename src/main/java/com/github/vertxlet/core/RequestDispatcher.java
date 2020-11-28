package com.github.vertxlet.core;

import com.github.vertxlet.core.spring.Controller;
import com.github.vertxlet.core.spring.RequestMapping;
import com.github.vertxlet.util.ReflectionUtils;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);
    private final Context ctx;
    private final Router router;

    public RequestDispatcher(Context ctx, Router router) {
        this.ctx = ctx;
        this.router = router;
    }

    public void scanControllers() throws Exception {
        final Reflections reflections = new Reflections("");

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Controller.class)) {
            String controllerUrl = clazz.getAnnotation(Controller.class).value();
            if (!controllerUrl.startsWith("/")) {
                throw new VertxletException("Path must be start with /");
            }

            Object controller = ReflectionUtils.loadClass(clazz);
            ReflectionUtils.inject(controller, ctx, Inject.class);

            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(RequestMapping.class)) continue;

                RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                String[] paths = mapping.path();
                HttpMethod[] httpMethods = mapping.method();

                for (String path : paths) {
                    if (!path.startsWith("/")) {
                        throw new VertxletException("Path must be start with /");
                    }
                    for (HttpMethod httpMethod : httpMethods) {
                        if (controllerUrl.equals("/")) {
                            controllerUrl = "";
                        }
                        router.route(httpMethod, controllerUrl + path)
                                .handler(rc -> invokeControllerMethod(controller, method, rc));
                        logger.info(String.format("Mapping %s:%s with %s.%s()",
                                httpMethod.toString(), controllerUrl + path,
                                clazz.getSimpleName(), method.getName()));
                    }
                }
            }
        }
    }

    private void invokeControllerMethod(Object controller, Method method, RoutingContext rc) {
        try {
            method.invoke(controller, rc);
        } catch (IllegalAccessException | InvocationTargetException e) {
            rc.fail(e);
        }
    }

    public Map<String, Vertxlet> scanVertxlet() throws Exception {
        final Reflections reflections = new Reflections("");
        final Map<String, Vertxlet> vertxletMap = new HashMap<>();

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(VertxletMapping.class)) {
            Vertxlet vertxlet = (Vertxlet) ReflectionUtils.loadClass(clazz);
            ReflectionUtils.inject(vertxlet, ctx, Inject.class);

            for (String url : clazz.getAnnotation(VertxletMapping.class).url()) {
                logger.info("Mapping url {} with class {}", url, clazz.getName());
                vertxletMap.put(url, vertxlet);
                router.route(url).handler(vertxlet);
            }
        }
        return vertxletMap;
    }
}
