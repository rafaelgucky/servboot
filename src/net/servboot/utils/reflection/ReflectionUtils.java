package net.servboot.utils.reflection;

import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.utils.strings.StringUtils;
import java.lang.reflect.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    public static boolean isDate(Class<?> clazz) {
        return clazz == Date.class || clazz == Instant.class || clazz == LocalDate.class || clazz == LocalDateTime.class || clazz == Timestamp.class || clazz == LocalTime.class;
    }

    public static boolean isTransient(Field field) {
        return field.toGenericString().contains("transient");
    }

    public static boolean isStatic(Field field) {
        return field.toGenericString().contains("static");
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

    public static void callSetter(Object obj, String propertyName, Object value) {
        callMethod(obj, propertyName, "set", value);
    }

    public static <T> T callGetter(Object obj, String propertyName) {
        if (propertyName.isBlank()) {
            return null;
        }

        try {
            if (propertyName.contains(".")) {
                String newPropertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1, propertyName.indexOf("."));
                Method method = getMethod(obj, newPropertyName, "get");
                Object newObj = method.invoke(obj);

                return callGetter(newObj, propertyName.substring(newPropertyName.length() + 1));
            } else {
                return (T) getMethod(obj, propertyName, "get").invoke(obj);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void callMethod(Object obj, String methodName, String methodPrefix, Object parameter) {
        if (methodName.isBlank()) {
            return;
        }

        try {
            if (methodName.contains(".")) {
                String newPropertyName = methodPrefix + methodName.substring(0, 1).toLowerCase() + methodName.substring(1, methodName.indexOf("."));
                Object newObj = Objects.requireNonNullElse(callGetter(obj, newPropertyName), instantiate(obj.getClass().getField(newPropertyName).getType()));
                callMethod(obj, newPropertyName, methodPrefix, newObj);
                callMethod(newObj, methodName.substring(newPropertyName.length() + 1), methodPrefix, parameter);
            } else {
                Method method = getMethod(obj, methodName, methodPrefix);
                method.invoke(obj, parameter);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    /**
     * Returns a class field
     * @param clazz base class
     * @param fieldName field name (it may contain dots)
     * @param searchLevel search level
     * @return field or throw exception
     * @throws NoSuchFieldException if field no found
     */
    public static Field getField(Class<?> clazz, String fieldName, int searchLevel) throws NoSuchFieldException {
        if (clazz == null || fieldName.isBlank()) return null;

        fieldName = StringUtils.removePrefix(fieldName, clazz.getSimpleName() + ".");

        if (fieldName.contains(".")) {
            String newFieldName = StringUtils.lowerFirst(fieldName.substring(0, fieldName.indexOf('.')));

            if (searchLevel > 1) {
                return getField(clazz.getField(newFieldName).getType(), fieldName.substring(fieldName.indexOf('.') + 1), --searchLevel);
            }

            return clazz.getField(newFieldName);
        }

        final String tempFieldName = fieldName;
        return ReflectionUtils.getAllFields(clazz).stream()
                .filter(f -> f.getName().equalsIgnoreCase(tempFieldName))
                .findAny()
                .get();
    }

    /**
     * Clone an object
     * @param obj
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj) {
        T newObj;
        Set<Field> properties = getAllFields(obj.getClass()).stream()
                .filter(p -> !isStatic(p) && !isTransient(p))
                .collect(Collectors.toSet());

        //todo

        if (1 == 1) {
            throw new RuntimeException("Method not implemented!");
        }
        try {
            newObj = (T) instantiate(obj.getClass());

            for (Field property : properties) {
                Object value = callGetter(obj, property.getName());
                Object newValue = value;

                if (value instanceof Iterable<?> iterableValue) {
                    newValue = new LinkedList<>();

                    for (Object item : iterableValue) {
                        callMethod(newValue, "add", "", item);
                    }
                }

                callSetter(newObj, property.getName(), newValue);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return newObj;
    }

    public static void copyTo(Object from, Object to, Set<Field> properties) {
        for (Field property : properties) {
            callSetter(to, property.getName(), callGetter(from, property.getName()));
        }
    }
}
