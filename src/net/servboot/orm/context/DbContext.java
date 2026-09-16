package net.servboot.orm.context;

import net.servboot.orm.DataSet;
import net.servboot.utils.reflection.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class DbContext {
    private final Map<Class<?>, DataSet<?>> entitiesContext;

    public DbContext() {
        entitiesContext = new LinkedHashMap<>();

        for (Field field : ReflectionUtils.getAllFields(getClass())) {
            entitiesContext.put(field.getType(), ReflectionUtils.callGetter(this, field.getName()));
        }
    }

    public Object getEntityContext(Class<?> entityClass) {
        return entitiesContext.get(entityClass);
    }
}
