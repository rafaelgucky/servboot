package net.servboot.orm;

import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Select <T> {
    private String command;
    private final Class<T> entityClass;
    private List<ColumnMap> columns;

    public Select(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public List<ColumnMap> getColumns() {
        return this.getColumns(this.getEntityClass(), "", "");
    }

    public List<ColumnMap> getColumns(Class<?> entityClass, String dbPrefix, String entityPrefix) {
        if (this.columns == null) {
            this.columns = new LinkedList<>();
        }

        if (this.columns.isEmpty()) {
            this.columns = this.generateColumns(entityClass, Object.class, dbPrefix, entityPrefix);
        }

        return this.columns;
    }

    public void setColumns(List<ColumnMap> columns) {
        this.columns = columns;
    }

    protected final List<ColumnMap> generateColumns(Class<?> entityClass, Class<?> parentClass, String dbPrefix, String entityPrefix) {
        Set<Field> fields = ReflectionUtils.getAllFields(entityClass).stream()
                .filter(field -> !ReflectionUtils.isStatic(field) && !ReflectionUtils.isTransient(field))
                .collect(Collectors.toSet());

        List<ColumnMap> columns = new LinkedList<>();

        if (dbPrefix == null || dbPrefix.isEmpty()) {
            dbPrefix = OrmReflectionUtils.getTableName(entityClass, false) + ".";
        }

        for (Field field : fields) {
            if (OrmReflectionUtils.isForeign(field)) {
                if (Objects.requireNonNull(OrmReflectionUtils.getForeignType(field)) != parentClass) {
                    columns.addAll(this.generateColumns(Objects.requireNonNull(OrmReflectionUtils.getForeignType(field)), entityClass, OrmReflectionUtils.getTableName(Objects.requireNonNull(OrmReflectionUtils.getForeignType(field)), false) + ".",  entityPrefix + Objects.requireNonNull(OrmReflectionUtils.getForeignType(field)).getSimpleName() + "."));
                }
            } else if (!ReflectionUtils.isTransient(field)) {
                columns.add(new ColumnMap(dbPrefix + OrmReflectionUtils.getDbFieldName(field), entityPrefix + field.getName()));
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

        command.append("select ");

        for (int i = 0; i < columns.size(); i++) {
            command.append(columns.get(i).getDbColumnName());
            command.append(" ");
            command.append(" as ");
            command.append("\"");
            command.append(columns.get(i).getEntityFieldName());
            command.append("\"");
            command.append(i + 1 < columns.size() ? ", \n " : " \n ");
        }

        command.append("from ");
        command.append(OrmReflectionUtils.getTableName(entityClass));
        command.append(" \n");

        return  command.toString();
    }
}
