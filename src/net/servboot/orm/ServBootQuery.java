package net.servboot.orm;

import net.servboot.utils.reflection.ColumnUtils;
import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ServBootQuery <T> {
    protected Class<T> entityClass;
    protected ServBootQuery<?> parent;
    protected List<Field> fields;
    protected List<Method> methods;
    private List<String> columns;

    public ServBootQuery(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public ServBootQuery(Class<T> entityClass, ServBootQuery<?> parent) {
        this.entityClass = entityClass;
        this.parent = parent;
    }

    public Class<T> getEntityClass() {
        return this.entityClass;
    }

    /**
     * Map witch columns we are queried
     * @param function Receives T, returns a new SerbBootQuery of K
     * @return a new ServBootQuery
     * @param <K> new entity
     */
    public <K> ServBootQuery<K> map(ServBootFunction<T, K> function) {
        try {
            Method method = function.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            String methodName = ((SerializedLambda) method.invoke(function)).getImplMethodName();
            String propName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
            Field field =  ReflectionUtils.getField(this.entityClass, propName);
            this.columns = List.of(propName);

            return ReflectionUtils.isPrimitive(field) ? new ServBootQuery<>((Class<K>) field.getType(), this) : new ServBootQuery<>((Class<K>) field.getType());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ServBootQuery<T> map(String[] columns) {
        this.columns.clear();
        for (String column : columns) {
            this.columns.add(ColumnUtils.getDataBaseName(this.entityClass, column));
        }

        return this;
    }

    public List<ColumnMap> getColumns() {
        return this.getColumns(this.getEntityClass(), "", "");
    }

    public List<ColumnMap> getColumns(Class<?> entityClass, String dbPrefix, String entityPrefix) {
        Set<Field> fields = ReflectionUtils.getAllFields(entityClass);
        List<ColumnMap> columns = new LinkedList<>();

        if (dbPrefix == null || dbPrefix.isEmpty()) {
            dbPrefix = OrmReflectionUtils.getTableName(entityClass) + ".";
        }

        for (Field field : fields) {
            if (OrmReflectionUtils.isForeign(field)) {
                columns.addAll(this.getColumns(field.getType(), OrmReflectionUtils.getTableName(field.getType()) + ".",  entityPrefix + field.getType().getSimpleName() + "."));
            } else if (!ReflectionUtils.isTransient(field)) {
                columns.add(new ColumnMap(dbPrefix + OrmReflectionUtils.getDbFieldName(field), entityPrefix + field.getName()));
            }
        }

        return columns;
    }
}
