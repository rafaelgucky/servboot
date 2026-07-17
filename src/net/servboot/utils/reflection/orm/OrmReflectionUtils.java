package net.servboot.utils.reflection.orm;

import net.servboot.annotations.Column;
import net.servboot.annotations.ForeignKey;
import net.servboot.annotations.Key;
import net.servboot.annotations.Table;
import net.servboot.annotations.enums.EntityLoad;
import net.servboot.utils.reflection.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class OrmReflectionUtils {
//    public static Map<String, Field> getFields(Class<?> entityClass) {
//
//    }

    public static String getTableName(Class<?> clazz){
        Table table = clazz.getAnnotation(Table.class);
        if(table == null || table.value().isEmpty()) return clazz.getSimpleName();
        return table.value();
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
        return ReflectionUtils.getAllFields(clazz).stream()
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

    public static Set<Field> getForeignFields(Class<?> clazz) {
        return ReflectionUtils.getAllFields(clazz).stream()
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

    public static boolean isForeign(Field field) {
        return field.getAnnotation(ForeignKey.class) != null;
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
}
