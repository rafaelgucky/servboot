package net.servboot.utils.reflection;

import net.servboot.annotations.Column;
import net.servboot.annotations.ForeignKey;
import net.servboot.annotations.Key;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ColumnUtils {
    public static boolean isForeign(Field field) {
        ForeignKey annotation = field.getAnnotation(ForeignKey.class);
        return annotation != null;
    }

    public static String getDataBaseName(Class<?> entityClass, String fieldName) {
        Field field;
        Column column;

        if (fieldName.contains(".")) {
            String newFieldName = fieldName.substring(0, fieldName.indexOf("."));
            Field newField      = ReflectionUtils.getField(entityClass, newFieldName);
            ForeignKey foreign;

            if ((foreign = newField.getAnnotation(ForeignKey.class)) != null) {
                if (!foreign.column().isEmpty()) {
                    return foreign.column();
                }

                Set<Field> keys = getKeys(newField.getType());

                if (keys.size() == 1) {
                    return newFieldName + "_" + keys.iterator().next().getName();
                }
            }

            return ColumnUtils.getDataBaseName(ReflectionUtils.getField(entityClass, newFieldName).getType(), fieldName.substring(fieldName.indexOf(".") + 1));
        }

        field = ReflectionUtils.getField(entityClass, fieldName);

        return (column = field.getAnnotation(Column.class)) != null ? column.name() : fieldName;
    }

    public static Set<Field> getKeys(Class<?> clazz) {
        return ReflectionUtils.getAllFields(clazz).stream()
                .filter(field -> Arrays.stream(field.getAnnotations())
                        .anyMatch(annotation -> annotation.annotationType().equals(Key.class)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
