package net.servboot.utils.reflection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InstantiationUtils {
    public static <T> T instantiate(Class<T> clazz, List<Field> fields, Map<String, Object> values) {
        try{
            Objects.requireNonNull(clazz);
            Objects.requireNonNull(values);

            Object obj = clazz.getDeclaredConstructor().newInstance();
            List<String> keys = values.keySet().stream().map(String::toLowerCase).toList();

            fields.forEach(field -> {
                try{
                    field.setAccessible(true);
                    if(keys.contains(field.getName().toLowerCase())) {
                        ReflectionUtils.setField(obj, field, values.get(field.getName().toLowerCase()));
                    }
                } catch(ClassCastException e){
                    throw new IllegalStateException(e.getMessage());
                }

            });

            return clazz.cast(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
