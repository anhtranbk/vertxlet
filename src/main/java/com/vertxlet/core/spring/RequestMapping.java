package com.vertxlet.core.spring;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequestMapping {

    String[] path() default "/";

    HttpMethod[] method() default {
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.DELETE,
            HttpMethod.HEAD,
            HttpMethod.PATCH,
            HttpMethod.OPTIONS,
            HttpMethod.TRACE,
            HttpMethod.CONNECT
    };
}
