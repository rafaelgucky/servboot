package net.servboot.utils.reflection.method;

import net.servboot.utils.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MethodUtils {
    public static Set<Method> getMethods(Class<?> clazz) {
        Set<Method> methods = new LinkedHashSet<>();
        if (clazz == null) return methods;

        methods.addAll(Arrays.stream(clazz.getDeclaredMethods()).toList());
        methods.addAll(getMethods(clazz.getSuperclass()));

        return methods;
    }

    public static Object[] getSortedParameters(Method method, Map<String, Object> parameters) {
        Parameter[] methodParameters = method.getParameters();
        Object[] parametersSorted = new Object[methodParameters.length];

        for (int i = 0; i < methodParameters.length; i++) {
            if (ReflectionUtils.isPrimitive(methodParameters[i].getType())) {
                parametersSorted[i] = ReflectionUtils.convertFromString(parameters.get(methodParameters[i].getName()).toString(), methodParameters[i].getType());
            } else {
                parametersSorted[i] = methodParameters[i].getType().cast(parameters.get(methodParameters[i].getName()));
            }
        }

        return parametersSorted;
    }
}
