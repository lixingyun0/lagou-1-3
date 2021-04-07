package com.xingyun.mvc.handler;

import java.lang.reflect.Method;
import java.util.Map;

public class Handler {

    private String url;

    private Method method;

    private Object controller;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }
}
