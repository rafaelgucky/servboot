package net.servboot.utils.reflection.method;

import net.servboot.utils.reflection.ReflectionUtils;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class MethodUtils {
    public static Set<Method> getMethods(Class<?> clazz) {
        Set<Method> methods = new LinkedHashSet<>();
        if (clazz == null) return methods;

        methods.addAll(Arrays.stream(clazz.getDeclaredMethods()).toList());
        methods.addAll(getMethods(clazz.getSuperclass()));

        return methods;
    }

    public static Object[] getSortedParameters(Method method, Map<String, Object> parameters, Map<String, List<File>> files) {
        Parameter[] methodParameters = method.getParameters();
        Object[] parametersSorted = new Object[methodParameters.length];

        for (int i = 0; i < methodParameters.length; i++) {
            if (ReflectionUtils.isPrimitive(methodParameters[i].getType())) {
                parametersSorted[i] = ReflectionUtils.convertFromString(parameters.get(methodParameters[i].getName()).toString(), methodParameters[i].getType());
            } else {
                if (files.containsKey(methodParameters[i].getName())) {
                    if (methodParameters[i].getType().equals(List.class)) {
                        parametersSorted[i] = files.get(methodParameters[i].getName());
                    } else {
                        parametersSorted[i] = files.get(methodParameters[i].getName()).getFirst();
                    }
                } else {
                    parametersSorted[i] = methodParameters[i].getType().cast(parameters.get(methodParameters[i].getName()));
                }
            }
        }

        return parametersSorted;
    }
}
