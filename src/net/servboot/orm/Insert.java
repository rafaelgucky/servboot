package net.servboot.orm;

import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Insert <T> {
    private final T entity;
    private List<InsertColumMap> columns;

    @SuppressWarnings("unchecked")
    public Insert(T entity) {
        this.entity = entity;
    }

    public List<InsertColumMap> getColumns() {
        if (this.columns == null) {
            this.columns = new LinkedList<>();
        }

        if (this.columns.isEmpty()) {
            this.columns = this.generateColumns();
        }

        return columns;
    }

    public List<InsertColumMap> generateColumns() {
        Set<Field> fields = ReflectionUtils.getAllFields(this.entity.getClass()).stream()
                .filter(field -> !ReflectionUtils.isStatic(field) && !ReflectionUtils.isTransient(field) && (!OrmReflectionUtils.isForeign(field) || OrmReflectionUtils.isOneToOne(field)))
                .collect(Collectors.toSet());

        List<InsertColumMap> columns = new LinkedList<>();

        if (1 == 1) {
            throw new RuntimeException("Ajustar a geração do sql de inserção. Não está colocando o nome do campo no banco corretamente");
        }

        try {
            for (Field field : fields) {
                if (OrmReflectionUtils.isOneToOne(field)) {
                    Class<?> foreignClass = OrmReflectionUtils.getForeignType(field);
                    for (Field foreignFieldKey : OrmReflectionUtils.getKeys(foreignClass)) {
                        columns.add(new InsertColumMap(OrmReflectionUtils.getDbFieldName(foreignFieldKey), foreignFieldKey.getName(), ReflectionUtils.callGetter(this.entity, field.getName() + "." + foreignFieldKey.getName())));
                    }

                } else {
                    columns.add(new InsertColumMap(OrmReflectionUtils.getDbFieldName(field), field.getName(), ReflectionUtils.callGetter(this.entity, field.getName())));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return columns;
    }

    public String getCommand() {
        StringBuilder command = new StringBuilder();

        command.append("INSERT INTO ");
        command.append(OrmReflectionUtils.getTableName(this.entity.getClass(), true));
        command.append(" (");

        for (int i = 0; i < this.getColumns().size(); i++) {
            command.append(this.getColumns().get(i).getDbColumnName());
            if (i < this.getColumns().size() - 1) {
                command.append(", ");
            }
        }

        command.append(") VALUES (");
        for (int i = 0; i < this.getColumns().size(); i++) {
            command.append("'").append(this.getColumns().get(i).getValue()).append("'");
            if (i < this.getColumns().size() - 1) {
                command.append(", ");
            }
        }

        command.append(")");
        return command.toString();
    }
}
