package net.servboot.dependency;

import java.lang.reflect.InvocationTargetException;

public interface IDependencyInjectionContainer {
    <T> void addApplicationScoped(Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException, RuntimeException;
    <T> T getApplicationScoped(Class<T> clazz);
    <T> void addRequestScoped(Class<T> clazz);
    <T> T getRequestScoped(Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException;
    <T> T loadInstance(Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException;;

}
