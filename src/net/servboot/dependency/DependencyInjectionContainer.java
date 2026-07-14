package net.servboot.dependency;

import net.servboot.utils.reflection.ReflectionUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DependencyInjectionContainer {
    private static final Map<Class<?>, Object> applicationScoped = new LinkedHashMap<>();
    private static final List<Class<?>> requestScoped = new LinkedList<>();

    public static <T> void addApplicationScoped(Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException, RuntimeException {
        if (requestScoped.contains(clazz)) throw new RuntimeException("Duplicate class on DI container detected");
        if (applicationScoped.containsKey(clazz)) throw new RuntimeException("Duplicate class on DI container detected");
        DependencyInjectionContainer.applicationScoped.put(clazz, ReflectionUtils.instantiate(clazz, false));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getApplicationScoped(Class<T> clazz) {
        return (T) DependencyInjectionContainer.applicationScoped.get(clazz);
    }

    public static <T> void addRequestScoped(Class<T> clazz) throws RuntimeException {
        if (requestScoped.contains(clazz)) throw new RuntimeException("Duplicate class on DI container detected");
        if (applicationScoped.containsKey(clazz)) throw new RuntimeException("Duplicate class on DI container detected");
        DependencyInjectionContainer.requestScoped.add(clazz);
    }

    public static <T> T getRequestScoped(Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (!requestScoped.contains(clazz)) return null;
        return ReflectionUtils.instantiate(clazz, false);
    }
}
