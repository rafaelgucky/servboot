package net.servboot.orm.context;

import net.servboot.utils.reflection.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DbContext {
    private final transient Map<Class<?>, DataSet<?>> entitiesContext;

    public DbContext() {
        entitiesContext = new LinkedHashMap<>();

        for (Field field : ReflectionUtils.getAllFields(getClass()).stream().filter(f -> !ReflectionUtils.isTransient(f)).collect(Collectors.toSet())) {
            entitiesContext.put(field.getType(), ReflectionUtils.callGetter(this, field.getName()));
        }
    }

    public Object getEntityContext(Class<?> entityClass) {
        return entitiesContext.get(entityClass);
    }
}
