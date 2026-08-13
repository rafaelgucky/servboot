package net.servboot.utils.reflection;

import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.utils.strings.StringUtils;
import java.lang.reflect.*;
import java.util.*;

public class ReflectionUtils {
    @SuppressWarnings("unchecked")
    public static <T> T convertFromString(Object value, Class<?> clazz){
        if(clazz.equals(boolean.class) || clazz.equals(Boolean.class)){
           return (T) Boolean.valueOf(value.toString());
        } else if(clazz.equals(char.class) || clazz.equals(Character.class)){
            return (T) Character.valueOf(value.toString().charAt(0));
        } else if(clazz.equals(byte.class) || clazz.equals(Byte.class)){
            return (T) Byte.valueOf(value.toString());
        } else if(clazz.equals(short.class) || clazz.equals(Short.class)){
            return (T) Short.valueOf(value.toString());
        } else if(clazz.equals(int.class) || clazz.equals(Integer.class)){
            return (T) Integer.valueOf(value.toString());
        }  else if(clazz.equals(long.class) || clazz.equals(Long.class)){
            return (T) Long.valueOf(value.toString());
        }  else if(clazz.equals(float.class) || clazz.equals(Float.class)){
            return (T) Float.valueOf(value.toString());
        }  else if(clazz.equals(double.class) || clazz.equals(Double.class)) {
            return (T) Double.valueOf(value.toString());
        } else {
            return (T) value;
        }
    }

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class
                || clazz == Character.class
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == String.class;
    }

    public static boolean isTransient(Field field) {
        return field.toGenericString().contains("transient");
    }

    public static boolean isPrimitive(Field field){
        return ReflectionUtils.isPrimitive(field.getType());
    }

    public static Set<Field> getAllFields(Class<?> clazz) {
        Set<Field> fields = new LinkedHashSet<>(Arrays.asList(clazz.getDeclaredFields()));

        if (clazz.getSuperclass() != Object.class) {
            fields.addAll(getAllFields(clazz.getSuperclass()));
        }

        return fields;
    }

    public static <T> T instantiate(Class<T> clazz)
            throws IllegalAccessException, InvocationTargetException, InstantiationException{
        return instantiate(clazz, false);
    }


    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> clazz, boolean fromDIContainer)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
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
            if (fromDIContainer) {
                if (DependencyInjectionContainer.getApplicationScoped(parameters[i].getType()) != null) {
                    instances[i] = (DependencyInjectionContainer.getApplicationScoped(parameters[i].getType()));
                } else if (DependencyInjectionContainer.getRequestScoped(parameters[i].getType()) != null) {
                    instances[i] = (DependencyInjectionContainer.getRequestScoped(parameters[i].getType()));
                } else {
                    throw new InstantiationException("class not found on DI Container");
                }
            } else {
                instances[i] = (ReflectionUtils.instantiate(parameters[i].getType(), false));
            }
        }

        return (T) constructs[0].newInstance(instances);
    }

    public static void callSetter(Object obj, String propertyName, Object value)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (propertyName.isBlank()) {
            return;
        }

        if (propertyName.contains(".")) {
            String newPropertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1, propertyName.indexOf("."));
            Object newObj = Objects.requireNonNullElse(callGetter(obj, newPropertyName), instantiate(obj.getClass().getField(newPropertyName).getType(), false));
            callSetter(obj, newPropertyName, newObj);
            callSetter(newObj, propertyName.substring(newPropertyName.length() + 1), value);
        } else {
            Method method = getMethod(obj, propertyName, "set");
            method.invoke(obj, value);
        }
    }

    public static <T> T callGetter(Object obj, String propertyName)
            throws IllegalAccessException, InvocationTargetException {
        if (propertyName.isBlank()) {
            return null;
        }

        if (propertyName.contains(".")) {
            String newPropertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1, propertyName.indexOf("."));
            Method method = getMethod(obj, newPropertyName, "get");
            Object newObj = method.invoke(obj);

            return callGetter(newObj, propertyName.substring(newPropertyName.length() + 1));
        } else {
            return (T) getMethod(obj, propertyName, "get").invoke(obj);
        }
    }

    public static Method getMethod(Object obj, String propertyName, String prefix) {
        String methodName = prefix + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        return Objects.requireNonNull(Arrays.stream(obj.getClass().getMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElse(null), "method \"" + methodName + "\" not found on class" + " [" + obj.getClass().getName() + "]");
    }

    public static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        return getField(clazz, fieldName, Integer.MAX_VALUE);
    }

    public static Field getField(Class<?> clazz, String fieldName, int searchLevel) throws NoSuchFieldException {
        if (clazz == null || fieldName.isBlank()) return null;

        if (fieldName.contains(".")) {
            String newFieldName = StringUtils.lowerFirst(fieldName.substring(0, fieldName.indexOf('.')));

            if (searchLevel > 1) {
                return getField(clazz.getField(newFieldName).getType(), fieldName.substring(fieldName.indexOf('.') + 1), --searchLevel);
            }

            return clazz.getField(newFieldName);
        }

        return clazz.getField(fieldName);
    }
}
