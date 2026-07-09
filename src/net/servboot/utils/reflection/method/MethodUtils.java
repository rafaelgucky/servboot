package net.servboot.utils.reflection.method;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class MethodUtils {
    public static Set<Method> getMethods(Class<?> clazz) {
        Set<Method> methods = new LinkedHashSet<>();
        if (clazz == null) return methods;

        methods.addAll(Arrays.stream(clazz.getDeclaredMethods()).toList());
        methods.addAll(getMethods(clazz.getSuperclass()));

        return methods;
    }
}
