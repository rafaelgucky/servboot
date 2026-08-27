package net.servboot.utils.reflection.orm;

import net.servboot.annotations.*;
import net.servboot.annotations.enums.EntityLoad;
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
        if(table == null || table.value().isEmpty()) return clazz.getSimpleName();
        return addSchema ? table.schema() + "." + table.value() : table.value();
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
                .filter(OrmReflectionUtils::isForeign)
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
        return field.getAnnotation(OneToMany.class) != null || field.getAnnotation(OneToOne.class) != null;
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
        List<Join> joins = new LinkedList<>();

        for (Field foreignField : getForeignFields(clazz)) {
            joins.add(generateJoin(clazz, foreignField));
        }

        return joins;
    }

    public static Join generateJoin(Class<?> parent, Field child) {
        Join join = null;

        OneToMany oneToMany = child.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            join = new Join(JoinType.LEFT_JOIN, getTableName(oneToMany.targetClass()));

            for (Field field : getKeys(OrmReflectionUtils.getForeignType(child))) {
                join.addCondition(
                        new Condition(
                                getTableName(oneToMany.targetClass(), false) + "." + getTableName(parent, false) + StringUtils.upperFirst(field.getName()),
                                Operator.EQUAL,
                                getTableName(parent, false) + "." + OrmReflectionUtils.getDbFieldName(field)
                        )
                );
            }
        } else {
            OneToOne oneToOne = child.getAnnotation(OneToOne.class);
            if (oneToOne != null) {
                join = new Join(JoinType.LEFT_JOIN, getTableName(oneToOne.targetClass() != null ?  oneToOne.targetClass() : child.getType()));

                for (Field field : getKeys(OrmReflectionUtils.getForeignType(child))) {
                    join.addCondition(new Condition( getTableName(parent) + "." + Objects.requireNonNull(OrmReflectionUtils.getForeignType(child)).getSimpleName().toLowerCase() + StringUtils.upperFirst(field.getName()), Operator.EQUAL, getTableName(Objects.requireNonNull(OrmReflectionUtils.getForeignType(child))) + "." + getDbFieldName(field)));
                }
            }
        }

        Objects.requireNonNull(join);

        return join;
    }

    public static <T> Field getFieldByJoinName(Class<T> clazz, String joinName) {
        Set<Field> fields = ReflectionUtils.getAllFields(clazz);

        for (Field field : fields) {
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (oneToMany != null && oneToMany.targetClass().getSimpleName().equalsIgnoreCase(joinName)) return field;
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
        boolean hasOneToMany = false;

        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);

            if (removePrefix.isBlank() || column.startsWith(removePrefix)) {
                if  (resultSet.getObject(i + 1) == null) {
                    continue;
                }

                // Se a propriedade não existir, é uma FK
                try {
                    Field field = ReflectionUtils.getField(entity.getClass(), StringUtils.removePrefix(column, removePrefix));
                    ReflectionUtils.callSetter(entity, StringUtils.removePrefix(column, removePrefix), getValue(resultSet, field.getType(), i + 1));
                } catch (NoSuchFieldException e) {
                    Field field = Objects.requireNonNull(getFieldByJoinName(entity.getClass(), column.substring(0, column.indexOf("."))));
                    Object fkEntity = null;
                    String rmPrefix = "";
                    hasOneToMany = true;

                    OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                    if (oneToMany != null) {
                        rmPrefix = oneToMany.targetClass().getSimpleName() + ".";
                        fkEntity = ReflectionUtils.instantiate(oneToMany.targetClass(), false);
                    }

                    fillEntityFromResultSet(fkEntity, resultSet, columns, rmPrefix);

                    Collection<Object> c = ReflectionUtils.callGetter(entity, field.getName());
                    if (c != null) {
                        c.add(fkEntity);
                    } else {
                        LinkedList<Object> list = new LinkedList<>();
                        list.add(fkEntity);
                        ReflectionUtils.callSetter(entity, field.getName(), list);
                    }

                    while (i < columns.size() && columns.get(i).toLowerCase().startsWith(removePrefix.toLowerCase())) {
                        i++;
                    }
                }
            }
        }

        if (hasOneToMany) {
            if (resultSet.next()) {
                if (compareKeys(entity, resultSet)) {
                    fillEntityFromResultSet(entity, resultSet, columns, removePrefix);
                }
            } else {
                resultSet.previous();
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

    public static <T> boolean compareKeys(T entity, ResultSet resultSet)
        throws SQLException, IllegalAccessException, InvocationTargetException {
        Set<Field> keys = getKeys(entity.getClass());

        if (true) {
            //throw new RuntimeException("Ajustar: o nome da coluna que virá do banco nem sempre será getDbFieldName(field)");
        }

        for (Field field : keys) {
            String columnName = getDbFieldName(field);

            if (ReflectionUtils.callGetter(entity, field.getName()) != resultSet.getObject(columnName)) {
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
