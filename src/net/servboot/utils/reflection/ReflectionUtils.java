package net.servboot.utils.reflection;

import net.servboot.annotations.*;
import net.servboot.annotations.enums.EntityLoad;
import net.servboot.controllers.ControllerBase;
import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.request.Request;
import net.servboot.utils.json.Json;
import java.io.File;
import java.lang.reflect.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectionUtils {
    public static List<Parameter> getMethodParameters(Method method){
        List<Parameter> parameters;
        if(method == null) return null;
        parameters = new LinkedList<>(Arrays.asList(method.getParameters()));
        return parameters;
    }

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

    public static Object instantiate(Class<?> clazz){
        Object result;

        try{
            List<Object> parameters = new LinkedList<>();
            Constructor<?>[] constructors = clazz.getConstructors();
            if(constructors.length > 1){ throw new RuntimeException("Only one constructor allowed for dependency injection"); }
            Parameter[] controllerParameters = constructors[0].getParameters();

            for(Parameter p : controllerParameters){
                Parameter[] subParameters = p.getType().getDeclaredConstructor().getParameters();
                if(subParameters.length > 0) {
                    parameters.add(instantiate(p.getType()));
                } else {
                    parameters.add(p.getType().getDeclaredConstructor().newInstance());
                }
            }

            result = switch (parameters.size()){
                case 0 -> clazz.getDeclaredConstructor().newInstance();
                case 1 -> clazz.getDeclaredConstructor(controllerParameters[0].getType())
                        .newInstance(parameters.getFirst());
                case 2 -> clazz.getDeclaredConstructor(controllerParameters[0].getType(), controllerParameters[1].getType())
                        .newInstance(parameters.getFirst(), parameters.get(1));
                case 3 -> clazz.getDeclaredConstructor(controllerParameters[0].getType(), controllerParameters[1].getType(), controllerParameters[2].getType())
                        .newInstance(parameters.getFirst(), parameters.get(1), parameters.get(2));
                case 4 -> clazz.getDeclaredConstructor(
                                controllerParameters[0].getType(),
                                controllerParameters[1].getType(),
                                controllerParameters[2].getType(),
                                controllerParameters[3].getType())
                        .newInstance(parameters.getFirst(),
                                    parameters.get(1),
                                    parameters.get(2),
                                    parameters.get(3));
                case 5 -> clazz.getDeclaredConstructor(
                                controllerParameters[0].getType(),
                                controllerParameters[1].getType(),
                                controllerParameters[2].getType(),
                                controllerParameters[3].getType(),
                                controllerParameters[4].getType())
                        .newInstance(parameters.getFirst(),
                                parameters.get(1),
                                parameters.get(2),
                                parameters.get(3),
                                parameters.get(4));
                case 6 -> clazz.getDeclaredConstructor(
                                controllerParameters[0].getType(),
                                controllerParameters[1].getType(),
                                controllerParameters[2].getType(),
                                controllerParameters[3].getType(),
                                controllerParameters[4].getType(),
                                controllerParameters[5].getType())
                        .newInstance(parameters.getFirst(),
                                parameters.get(1),
                                parameters.get(2),
                                parameters.get(3),
                                parameters.get(4),
                                parameters.get(5));
                case 7 -> clazz.getDeclaredConstructor(
                                controllerParameters[0].getType(),
                                controllerParameters[1].getType(),
                                controllerParameters[2].getType(),
                                controllerParameters[3].getType(),
                                controllerParameters[4].getType(),
                                controllerParameters[5].getType(),
                                controllerParameters[6].getType())
                        .newInstance(parameters.getFirst(),
                                parameters.get(1),
                                parameters.get(2),
                                parameters.get(3),
                                parameters.get(4),
                                parameters.get(5),
                                parameters.get(6));
                case 8 -> clazz.getDeclaredConstructor(
                                controllerParameters[0].getType(),
                                controllerParameters[1].getType(),
                                controllerParameters[2].getType(),
                                controllerParameters[3].getType(),
                                controllerParameters[4].getType(),
                                controllerParameters[5].getType(),
                                controllerParameters[6].getType(),
                                controllerParameters[7].getType())
                        .newInstance(parameters.getFirst(),
                                parameters.get(1),
                                parameters.get(2),
                                parameters.get(3),
                                parameters.get(4),
                                parameters.get(5),
                                parameters.get(6),
                                parameters.get(7));
                case 9 -> clazz.getDeclaredConstructor(
                                controllerParameters[0].getType(),
                                controllerParameters[1].getType(),
                                controllerParameters[2].getType(),
                                controllerParameters[3].getType(),
                                controllerParameters[4].getType(),
                                controllerParameters[5].getType(),
                                controllerParameters[6].getType(),
                                controllerParameters[7].getType(),
                                controllerParameters[8].getType())
                        .newInstance(parameters.getFirst(),
                                parameters.get(1),
                                parameters.get(2),
                                parameters.get(3),
                                parameters.get(4),
                                parameters.get(5),
                                parameters.get(6),
                                parameters.get(7),
                                parameters.get(8));
                case 10 -> clazz.getDeclaredConstructor(
                                controllerParameters[0].getType(),
                                controllerParameters[1].getType(),
                                controllerParameters[2].getType(),
                                controllerParameters[3].getType(),
                                controllerParameters[4].getType(),
                                controllerParameters[5].getType(),
                                controllerParameters[6].getType(),
                                controllerParameters[7].getType(),
                                controllerParameters[8].getType(),
                                controllerParameters[9].getType())
                        .newInstance(parameters.getFirst(),
                                parameters.get(1),
                                parameters.get(2),
                                parameters.get(3),
                                parameters.get(4),
                                parameters.get(5),
                                parameters.get(6),
                                parameters.get(7),
                                parameters.get(8),
                                parameters.get(9));
                default -> null;
            };
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return result;
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

























    public static boolean isNotNull(Field field){
        ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
        if(foreignKey == null){
            Column column = field.getAnnotation(Column.class);
            if(column == null) return true;
            else {
                return column.notNull();
            }
        } else {
            return foreignKey.notNull();
        }
    }

    public static boolean isIncrement(Field field) {
        Key annotation = field.getAnnotation(Key.class);
        if(annotation == null) return false;
        return annotation.increment();
    }

    /**
     * @deprecated
     * @param field
     * @return
     */
    public static boolean isForeign(Field field) {
        ForeignKey annotation = field.getAnnotation(ForeignKey.class);
        if(annotation == null) return false;
        return true;
    }

    public static boolean isEager(Field field) {
        ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
        if(foreignKey == null){
            Column column = field.getAnnotation(Column.class);
            if(column == null) return true;
            else {
                return column.load() == EntityLoad.EAGER;
            }
        } else {
            return foreignKey.load() == EntityLoad.EAGER;
        }
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

    public static Set<Field> getForeignFields(Class<?> clazz) {
        return getAllFields(clazz).stream()
                .filter(field -> field.isAnnotationPresent(ForeignKey.class))
                .collect(Collectors.toSet());
    }

    public static Set<Field> getEagerFields(Class<?> clazz) {
        Set<Field> fields = new LinkedHashSet<>();

        for(Field field : clazz.getDeclaredFields()){
            Column column = field.getAnnotation(Column.class);
            if(column != null && column.load() == EntityLoad.LAZY) continue;
            fields.add(field);
        }

        for(Field field : clazz.getFields()){
            Column column = field.getAnnotation(Column.class);
            if(column != null && column.load() == EntityLoad.LAZY) continue;
            fields.add(field);
        }

        return fields;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        return getAllFields(clazz).stream()
                .filter(f -> f.getName().equalsIgnoreCase(fieldName))
                .findFirst().orElse(null);
    }

    public static Class<?> getForeignClazz(Field field){
        return field.getAnnotation(ForeignKey.class).entity();
    }

    public static Field getForeignField(Class<?> clazz, String fieldName) {
        return getKeys(clazz).stream()
                .filter(f -> f.getName().equalsIgnoreCase(fieldName))
                .findFirst().orElse(null);
    }

    public static String getForeignFieldName(Field field) {
        return field.getAnnotation(ForeignKey.class).column();
    }

    public static Set<Field> getKeys(Class<?> clazz) {
        return getAllFields(clazz).stream()
                .filter(field -> Arrays.stream(field.getAnnotations())
                        .anyMatch(annotation -> annotation.annotationType().equals(Key.class)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    public static String getDbFieldName(Field field){
        Column column = field.getAnnotation(Column.class);
        return column != null && !column.name().isEmpty() ? column.name() : field.getName().toLowerCase();
    }

    public static Field getFieldFromDbColumn(Set<Field> fields, String columnName){
        for(Field field : fields){
            if(field.getName().equalsIgnoreCase(columnName) || getDbFieldName(field).equalsIgnoreCase(columnName)){
                return field;
            }
        }

        return null;
    }

    public static void setField(Object obj, Field field, Object value){
        try{
            switch (field.getType().getSimpleName()) {
                case "boolean":
                    if(value != null){
                        field.setBoolean(obj, (boolean) value);
                    } else {
                        field.setBoolean(obj, false);
                    }
                    break;
                case "char":
                    if(value != null){
                        field.setChar(obj, (char) value);
                    } else {
                        field.setChar(obj, (char) 0x0);
                    }
                    break;
                case "byte":
                    if(value != null){
                        field.setByte(obj, (byte) value);
                    } else {
                        field.setByte(obj, (byte) 0);
                    }
                    break;
                case "short":
                    if(value != null){
                        field.setShort(obj, (short) value);
                    } else {
                        field.setShort(obj, (short) 0);
                    }
                    break;
                case "int":
                    if(value != null){
                        field.setInt(obj, (int) value);
                    } else {
                        field.setInt(obj, 0);
                    }
                    break;
                case "long":
                    if(value != null){
                        field.setLong(obj, (long) value);
                    } else {
                        field.setLong(obj, 0L);
                    }
                    break;
                case "float":
                    if(value != null){
                        field.setFloat(obj, (float) value);
                    } else {
                        field.setFloat(obj, 0F);
                    }
                    break;
                case "double":
                    if(value != null){
                        field.setDouble(obj, (double) value);
                    } else {
                        field.setDouble(obj, 0D);
                    }
                    break;
                default:
                    field.set(obj, field.getType().cast(value));
            }
        } catch(IllegalAccessException | ClassCastException e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    // ==================== Entity ======================= //

    public static String getTableName(Class<?> clazz){
        Table table = clazz.getAnnotation(Table.class);
        if(table == null || table.value().isEmpty()) return clazz.getSimpleName();
        return table.value();
    }

    public static <T> List<Object> getValues(T object, Set<Field> fields){
        List<Object> values;

        if(object == null ||  fields == null || fields.isEmpty()){
            return Collections.emptyList();
        }

        values = new LinkedList<>();

        try {
            for(Field field : fields){
                field.setAccessible(true);
                values.add(field.get(object));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return values;
    }

    public static Map<Field, Object> getFieldsValue(Object obj){
        return getFieldsValue(obj, getAllFields(obj.getClass()));
    }

    public static Map<Field, Object> getFieldsValue(Object obj, Set<Field> fields){
        if(obj == null || fields == null){
            return null;
        }

        Map<Field, Object> values = new LinkedHashMap<>();

        try{
            for(Field field : fields){
                field.setAccessible(true);
                values.put(field, field.get(obj));
            }

            return values;
        } catch(IllegalAccessException | ClassCastException e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    // ======================== Generic ================== //

    public static boolean isPrimitive(Object value){
        return ReflectionUtils.isPrimitive(value.getClass());
    }












    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> clazz, boolean fromDIContainer) throws IllegalAccessException, InvocationTargetException, InstantiationException {
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
                instances[i] = (ReflectionUtils.instantiate(parameters[i].getType()));
            }
        }

        return (T) constructs[0].newInstance(instances);
    }
}
