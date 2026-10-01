package net.servboot.orm;

import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import net.servboot.utils.strings.StringUtils;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Select <T> {
    public static final String BASE_COMMAND = "select";

    private String command;
    private final Class<T> entityClass;
    private List<ColumnMap> columns;
    private final List<Join> joins;

    public Select(Class<T> entityClass) {
        this(entityClass, null);
    }

    public Select(Class<T> entityClass, List<Join> joins) {
        this.entityClass = entityClass;
        this.joins = joins;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public List<ColumnMap> getColumns() {
        if (this.columns == null) {
            this.columns = new LinkedList<>();
        }

        if (this.columns.isEmpty()) {
            this.columns = this.generateColumns();
        }

        return this.columns;
    }

    public void setColumns(List<ColumnMap> columns) {
        this.columns = columns;
    }

    public static <T> Select<T> of(Class<T> entityClass) {
        return new Select<>(entityClass);
    }

    public static <T> Select<T> of(Class<T> entityClass, List<Join> joins) {
        Select<T> select = new Select<>(entityClass, joins);
        select.setColumns(select.generateColumns());

        return select;
    }

    private List<ColumnMap> generateColumns() {
        List<ColumnMap> columns = new LinkedList<>();
        Set<Field> fields = ReflectionUtils.getAllFields(entityClass).stream()
                .filter(field -> !ReflectionUtils.isStatic(field) && !ReflectionUtils.isTransient(field) && !OrmReflectionUtils.isForeign(field))
                .collect(Collectors.toSet());

        for (Field field : fields) {
            columns.add(new ColumnMap(OrmReflectionUtils.getTableName(this.getEntityClass()) + "." + OrmReflectionUtils.getDbFieldName(field), field.getName()));
        }

        for (Join join : joins) {
            Set<Field> joinFields = ReflectionUtils.getAllFields(join.getEntityType()).stream()
                    .filter(field -> !ReflectionUtils.isStatic(field) && !ReflectionUtils.isTransient(field) && !OrmReflectionUtils.isForeign(field))
                    .collect(Collectors.toSet());

            for (Field field : joinFields) {
                columns.add(new ColumnMap(
                        StringUtils.concat(join.getAlias(), ".", OrmReflectionUtils.getDbFieldName(field)),
                        StringUtils.concat(join.getPropertyName(), ".", field.getName()))
                );
            }
        }

        return columns;
    }

    public boolean removeColumn(String entityName) {
        return this.getColumns().removeIf(column -> column.getEntityFieldName().equalsIgnoreCase(entityName));
    }

    public void setCommand(String command) {
        this.command = command;
    }


    public String getCommand() {
        if (this.command != null) {
            return this.command;
        }

        List<ColumnMap> columns = this.getColumns();
        StringBuilder command = new StringBuilder();

        command.append(StringUtils.concat(BASE_COMMAND, " "));

        for (int i = 0; i < columns.size(); i++) {
            command.append(columns.get(i).getCommand());
            command.append(i + 1 < columns.size() ? ", \n " : " \n ");
        }

        command.append("from ");
        command.append(OrmReflectionUtils.getTableName(entityClass));
        command.append(" \n");

        return command.toString();
    }
}
