package net.servboot.utils.reflection.orm;

import net.servboot.annotations.*;
import net.servboot.orm.Condition;
import net.servboot.orm.Join;
import net.servboot.orm.enums.JoinType;
import net.servboot.orm.enums.Operator;
import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.strings.StringUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class OrmReflectionUtils {

    public static String getTableName(Class<?> clazz){
        return getTableName(clazz, true);
    }

    public static String getTableName(Class<?> clazz, boolean addSchema){
        Table table = clazz.getAnnotation(Table.class);
        if(table == null || table.tableName().isEmpty()) return clazz.getSimpleName();
        return addSchema ? table.schema() + "." + table.tableName() : table.tableName();
    }

    public static Field getForeignField(Class<?> clazz, String fieldName) {
        return getKeys(clazz).stream()
                .filter(f -> f.getName().equalsIgnoreCase(fieldName))
                .findFirst().orElse(null);
    }

    public static Set<Field> getKeys(Class<?> clazz) {
        return ReflectionUtils.getAllFields(clazz).stream()
                .filter(field -> Arrays.stream(field.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(Key.class)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<String> getKeysAsString(Class<?> clazz) {
        return getKeysAsString(clazz, "");
    }

    public static Set<String> getKeysAsString(Class<?> clazz, String prefix) {
        Set<String> stringKeys = new LinkedHashSet<>();
        Set<Field> keys = ReflectionUtils.getAllFields(clazz).stream()
                .filter(field -> Arrays.stream(field.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(Key.class)))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Field field : keys) {
            if (isForeign(field)) {
                stringKeys.addAll(getKeysAsString(getForeignType(field), prefix + field.getName() + "."));
            } else {
                stringKeys.add(prefix + field.getName());
            }
        }

        return stringKeys;
    }

    public static String getDbFieldName(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column != null && !column.name().isEmpty() ? column.name() : field.getName().toLowerCase();
    }

    public static Field getFieldByDbColumnName(Set<Field> fields, String columnName){
        for(Field field : fields){
            if(field.getName().equalsIgnoreCase(columnName) || getDbFieldName(field).equalsIgnoreCase(columnName)){
                return field;
            }
        }

        return null;
    }

    public static Set<Field> getForeignFields(Class<?> clazz) {
        return ReflectionUtils.getAllFields(clazz).stream()
                .filter(f -> isForeign(f) && !ReflectionUtils.isTransient(f) && !ReflectionUtils.isStatic(f))
                .collect(Collectors.toSet());
    }

    public static boolean isIncrement(Field field) {
        Key annotation = field.getAnnotation(Key.class);
        if(annotation == null) return false;
        return annotation.increment();
    }

    public static boolean isForeign(Field field) {
        return isOneToMany(field) || isOneToOne(field);
    }

    public static boolean isOneToOne(Field field) {
        return field.getAnnotation(OneToOne.class) != null;
    }

    public static boolean isOneToMany(Field field) {
        return field.getAnnotation(OneToMany.class) != null;
    }

    public static Class<?> getForeignType(Field field) {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        if (oneToOne != null) {
            return oneToOne.targetClass() != null ? oneToOne.targetClass() : field.getType();
        }

        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            return oneToMany.targetClass();
        }

        return null;
    }

    public static List<Join> getJoins(Class<?> clazz) {
        return getJoins(clazz, Object.class, null, null);
    }

    public static List<Join> getJoins(Class<?> clazz, Class<?> parentClass, Join parentJoin, String alias) {
        List<Join> joins = new LinkedList<>();

        for (Field foreignField : getForeignFields(clazz)) {
            Class<?> foreignType = Objects.requireNonNull(getForeignType(foreignField));
            alias = parentClass == Object.class ? foreignField.getName().toLowerCase() : "\"" + alias.replace("\"", "") + "_" + foreignField.getName().toLowerCase() + "\"";

            Join join = new Join(foreignType, JoinType.LEFT_JOIN, getTableName(foreignType), alias, parentJoin);

            buildJoin(join, clazz, foreignField);
            joins.add(join);

            if (!foreignType.equals(parentClass)) {
                joins.addAll(getJoins(foreignType, clazz, join, alias));
            }
        }

        return joins;
    }

    public static void buildJoin(Join join, Class<?> parent, Field child) {
        Class<?> foreignType = Objects.requireNonNull(getForeignType(child));

        OneToMany oneToMany = child.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            for (Field field : getKeys(OrmReflectionUtils.getForeignType(child))) {
                join.addCondition(
                        new Condition(
                                join.getAlias() + "." + getTableName(parent, false) + StringUtils.upperFirst(field.getName()),
                                Operator.EQUAL,
                                (join.getAliasParent() != null ? join.getAliasParent() : OrmReflectionUtils.getTableName(parent)) + "." + OrmReflectionUtils.getDbFieldName(field)
                        )
                );
            }
        } else {
            OneToOne oneToOne = child.getAnnotation(OneToOne.class);
            if (oneToOne != null) {
                for (Field field : getKeys(foreignType)) {
                    join.addCondition(new Condition(
                            join.getAlias() + "." + getDbFieldName(field),
                            Operator.EQUAL,
                            (join.getAliasParent() != null ? join.getAliasParent() : OrmReflectionUtils.getTableName(parent)) + "." + Objects.requireNonNull(OrmReflectionUtils.getForeignType(child)).getSimpleName().toLowerCase() + StringUtils.upperFirst(field.getName())
                            )
                    );
                }
            }
        }
    }

    public static <T> Field getFieldByJoinName(Class<T> clazz, String joinName) {
        Set<Field> fields = ReflectionUtils.getAllFields(clazz);

        for (Field field : fields) {
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (oneToMany != null && oneToMany.targetClass().getSimpleName().equalsIgnoreCase(joinName)) return field;

            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            if (oneToOne != null
                    && (oneToOne.targetClass() != void.class && oneToOne.targetClass().getSimpleName().equalsIgnoreCase(joinName))
                    || field.getType().getSimpleName().equalsIgnoreCase(joinName)) {
                return field;
            }
        }

        return null;
    }

    public static <T> List<T> getAllEntitiesByResultSet(Class<T> entityClass, ResultSet resultSet)
            throws SQLException, NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException {
        LinkedList<T> entities = new LinkedList<>();

        while (resultSet.next()) {
            T entity = ReflectionUtils.instantiate(entityClass);
            fillEntityFromResultSet(entity, resultSet);
            entities.add(entity);
        }

        return entities;
    }

    public static <T> void fillEntityFromResultSet(T entity, ResultSet resultSet)
        throws SQLException, NoSuchFieldException,IllegalAccessException, InvocationTargetException, InstantiationException {
        fillEntityFromResultSet(entity, resultSet, getQueriedColumns(resultSet));
    }

    public static <T> void fillEntityFromResultSet(T entity, ResultSet resultSet, List<String> columns)
        throws SQLException, NoSuchFieldException,IllegalAccessException, InvocationTargetException, InstantiationException {
        fillEntityFromResultSet(entity, resultSet, columns, "");
    }

    public static <T> void fillEntityFromResultSet(T entity, ResultSet resultSet, List<String> columns, String removePrefix)
            throws SQLException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);

            if (removePrefix.isBlank() || column.startsWith(removePrefix)) {
                column = StringUtils.removePrefix(column, removePrefix);

                if  (resultSet.getObject(i + 1) == null) {
                    continue;
                }

                // Se for um tipo composto
                if (column.contains(".")) {
                    String property = column.substring(0, column.indexOf("."));
                    Field foreignField = ReflectionUtils.getField(entity.getClass(), property);
                    Class<?> foreignType = OrmReflectionUtils.getForeignType(foreignField);
                    Object newEntity = isOneToMany(foreignField) ? ReflectionUtils.instantiate(foreignType) : Objects.requireNonNullElseGet(ReflectionUtils.callGetter(entity, property), () -> {
                                try {
                                    return ReflectionUtils.instantiate(foreignType);
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                    );

                    fillEntityFromResultSet(newEntity, resultSet, columns, StringUtils.concat(removePrefix, property, "."));

                    while (i < columns.size() && columns.get(i).toLowerCase().startsWith(removePrefix.toLowerCase())) {
                        i++;
                    }
                    i--; // Remove loop for auto incrementation

                    if (isOneToMany(foreignField)) {
                        Collection<Object> list = ReflectionUtils.callGetter(entity, property);
                        if (list == null) {
                            list = new LinkedList<>();
                            ReflectionUtils.callSetter(entity, property, list);
                        }
                        list.add(newEntity);

                        if (resultSet.next() && compareKeys(entity, resultSet, removePrefix)) {
                            fillEntityFromResultSet(entity, resultSet, columns, removePrefix);
                        } else {
                            resultSet.previous();
                        }
                    } else if (isOneToOne(foreignField)) {
                        ReflectionUtils.callSetter(entity, property, newEntity);
                    }
                } else {
                    Field field = ReflectionUtils.getField(entity.getClass(), StringUtils.removePrefix(column, removePrefix));
                    ReflectionUtils.callSetter(entity, StringUtils.removePrefix(column, removePrefix), getValue(resultSet, field.getType(), i + 1));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getValue(ResultSet resultSet, Class<T> clazz, int index) throws SQLException {

        // Date
        if (clazz == Date.class) {
            return (T) resultSet.getDate(index);
        } else if (clazz == Timestamp.class) {
            return (T) resultSet.getTimestamp(index);
        }  else if (clazz == LocalDate.class) {
            return (T) resultSet.getDate(index).toLocalDate();
        }  else if (clazz == LocalTime.class) {
            return (T) resultSet.getTime(index).toLocalTime();
        }   else if (clazz == LocalDateTime.class) {
            return (T) resultSet.getTimestamp(index).toLocalDateTime();
        } else if (clazz == Instant.class) {
            return (T) resultSet.getDate(index).toInstant();
        }

        // Primitives
        else if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) (Boolean) resultSet.getBoolean(index);
        } else if (clazz == Character.class || clazz == char.class) {
            return (T) (Character) resultSet.getString(index).charAt(0);
        } else if (clazz == Byte.class || clazz == byte.class) {
            return (T) (Byte) resultSet.getByte(index);
        } else if (clazz == Short.class || clazz == short.class) {
            return (T) (Short) resultSet.getShort(index);
        } else if (clazz == Integer.class || clazz == int.class) {
            return (T) (Integer) resultSet.getInt(index);
        } else if (clazz == Long.class || clazz == long.class) {
            return (T) (Long) resultSet.getLong(index);
        }  else if (clazz == Float.class || clazz == float.class) {
            return (T) (Float) resultSet.getFloat(index);
        } else if (clazz == Double.class || clazz == double.class) {
            return (T) (Double) resultSet.getDouble(index);
        } else if (clazz == BigDecimal.class) {
            return (T) resultSet.getBigDecimal(index);
        } else if (clazz == String.class) {
            return (T) resultSet.getString(index);
        } else {
            return (T) resultSet.getObject(index);
        }
    }

    public static <T> boolean compareKeys(T entity, ResultSet resultSet, String prefix)
        throws SQLException, NoSuchFieldException {
        Set<String> keys = getKeysAsString(entity.getClass());

        for (String stringKey : keys) {
            Field field = ReflectionUtils.getField(entity.getClass(), stringKey);
            String columnName = getDbFieldName(field);

            if (ReflectionUtils.callGetter(entity, stringKey) != resultSet.getObject(StringUtils.concat(prefix, columnName))) {
                return false;
            }
        }

        return true;
    }

    public static <T> boolean equals(T entity, T other) throws IllegalAccessException, InvocationTargetException {
        Set<Field> keys = getKeys(entity.getClass());

        for (Field field : keys) {
            Object entityValue = ReflectionUtils.callGetter(entity, field.getName());
            Object otherValue = ReflectionUtils.callGetter(other, field.getName());

            if ((entityValue == null && otherValue == null) || entityValue != otherValue) {
                return false;
            }
        }

        return true;
    }

    public static List<String> getQueriedColumns(ResultSet resultSet) throws SQLException {
        List<String> columns = new LinkedList<>();

        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
            columns.add(resultSet.getMetaData().getColumnName(i));
        }

        return columns;
    }
}
