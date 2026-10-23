package net.servboot.dependency;

import net.servboot.client.ClientRequestTask;
import net.servboot.utils.reflection.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

public class DependencyInjectionContainer implements IDependencyInjectionContainer {
    private static DependencyInjectionContainer instance;
    private final Map<Class<?>, Object> applicationScoped;
    private final List<Class<?>> requestScoped;

    private DependencyInjectionContainer() {
        this.applicationScoped = new LinkedHashMap<>();
        this.requestScoped = new LinkedList<>();
    }

    public static DependencyInjectionContainer getInstance() {
        if (instance == null) {
            instance = new DependencyInjectionContainer();
        }

        return instance;
    }

    @Override
    public <T> void addApplicationScoped(Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (requestScoped.contains(clazz)) throw new RuntimeException("Duplicate class on DI container detected");
        if (applicationScoped.containsKey(clazz)) throw new RuntimeException("Duplicate class on DI container detected");
        this.applicationScoped.put(clazz, ReflectionUtils.instantiate(clazz));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getApplicationScoped(Class<T> clazz) {
        return (T) this.applicationScoped.get(clazz);
    }

    @Override
    public <T> void addRequestScoped(Class<T> clazz) {
        if (requestScoped.contains(clazz)) throw new RuntimeException("Duplicate class on DI container detected");
        if (applicationScoped.containsKey(clazz)) throw new RuntimeException("Duplicate class on DI container detected");
        this.requestScoped.add(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getRequestScoped(Class<T> clazz) {
        if (!requestScoped.contains(clazz)) return null;

        if (Thread.currentThread() instanceof ClientRequestTask thread) {
            if (thread.getLocal("dependencyInjectionContainer") == null) {
                thread.addLocal("dependencyInjectionContainer", new LinkedHashMap<Class<?>, Object>());
            }

            if (thread.getLocal("dependencyInjectionContainer") instanceof Map<?,?> dependencyInjectionContainer) {
                if (dependencyInjectionContainer.containsKey(clazz)) {
                    return (T) dependencyInjectionContainer.get(clazz);
                }
            }

        }

        return null;
    }

    @Override
    public <T> T loadInstance(Class<T> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (this.getApplicationScoped(clazz) != null) {
            return this.getApplicationScoped(clazz);
        } else if (this.requestScoped.contains(clazz)) {
            return Objects.requireNonNullElseGet(this.getRequestScoped(clazz), () -> {
                try {
                    return this.instantiate(clazz);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return instantiate(clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiate(Class<T> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?>[] constructs = clazz.getDeclaredConstructors();
        Parameter[] parameters;
        Object[] instances;

        if (constructs.length != 1) return null;

        parameters = constructs[0].getParameters();
        instances = new Object[parameters.length];

        if (parameters.length == 0) {
            return (T) constructs[0].newInstance();
        }

        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterType = parameters[i].getType();
            Object parameter = this.getApplicationScoped(parameterType);

            if (parameter == null) {
                parameter = this.getRequestScoped(parameterType);
            }

            if (parameter == null) {
                parameter = this.instantiate(parameterType);
            }

            instances[i] = parameter;
        }

        return (T) constructs[0].newInstance(instances);
    }
}
