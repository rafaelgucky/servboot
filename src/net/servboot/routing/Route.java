package net.servboot.routing;

import net.servboot.test.PersonController;
import net.servboot.test.PersonService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Route {
    private final String path;
    private final Class<?> controller;
    private final Method method;

    public Route(String path, Class<?> controller, Method method) {
        this.path = path;
        this.controller = controller;
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public Class<?> getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }


    public Object call(Object... params) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return method.invoke(controller, 1);
    }
}
