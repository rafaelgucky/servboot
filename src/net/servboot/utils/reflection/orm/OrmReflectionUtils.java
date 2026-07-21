package net.servboot.utils.reflection.orm;

import com.mysql.cj.jdbc.result.ResultSetImpl;
import net.servboot.annotations.Column;
import net.servboot.annotations.ForeignKey;
import net.servboot.annotations.Key;
import net.servboot.annotations.Table;
import net.servboot.annotations.enums.EntityLoad;
import net.servboot.orm.Condition;
import net.servboot.orm.Join;
import net.servboot.orm.enums.JoinType;
import net.servboot.orm.enums.Operator;
import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.strings.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class OrmReflectionUtils {

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

    public static List<Join> getJoins(Class<?> clazz) {
        List<Join> joins = new LinkedList<>();

        for (Field foreignField : getForeignFields(clazz)) {
            joins.add(generateJoin(clazz, foreignField));
        }

        return joins;
    }

    public static Join generateJoin(Class<?> parent, Field child) {
        ForeignKey foreignKey = Objects.requireNonNull(child.getAnnotation(ForeignKey.class));
        Join join = new Join(foreignKey.notNull() ? JoinType.INNER_JOIN : JoinType.LEFT_JOIN, getTableName(getForeignClazz(child)));

        for (Field field : getKeys(child.getType())) {
            join.addCondition(new Condition( getTableName(parent) + "." + child.getType().getSimpleName().toLowerCase() + StringUtils.upperFirst(field.getName()), Operator.EQUAL, getTableName(child.getType()) + "." + getDbFieldName(field)));
        }

        return join;
    }

    public static <T> void fillEntityFromResultSet(T entity, ResultSet resultSet)
            throws SQLException, NoSuchFieldException,IllegalAccessException, InvocationTargetException, InstantiationException {
        List<String> columns = getQueriedColumns(resultSet);

        fillEntityFromResultSet(entity, resultSet, columns);
    }

    public static <T> List<T> getAllEntitiesFromResultSet(Class<T> entityClass, ResultSet resultSet)
            throws SQLException, NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException {
        List<T> entities = new LinkedList<>();
        List<String> columns;

        if (!resultSet.next()) return entities;

        columns = getQueriedColumns(resultSet);

        do {
            T entity = Objects.requireNonNull(ReflectionUtils.instantiate(entityClass, false));
            fillEntityFromResultSet(entity, resultSet, columns);
            entities.add(entity);
        } while (resultSet.next());

        return entities;
    }

    public static <T> void fillEntityFromResultSet(T entity, ResultSet resultSet, List<String> columns)
        throws SQLException, NoSuchFieldException,IllegalAccessException, InvocationTargetException, InstantiationException {
        for (String column : columns) {
            Object value = resultSet.getObject(column);

            if (value != null) {
                ReflectionUtils.callSetter(entity, column, resultSet.getObject(column));
            }
        }
    }

    public static List<String> getQueriedColumns(ResultSet resultSet) {
        return Arrays.stream(((ResultSetImpl) resultSet).getMetadata().getFields())
                .map(com.mysql.cj.result.Field::getName)
                .toList();
    }
}
