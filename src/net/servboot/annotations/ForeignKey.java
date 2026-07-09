package net.servboot.annotations;

import net.servboot.annotations.enums.EntityLoad;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
    Class<?> entity();
    String column() default "";
    boolean notNull() default false;
    EntityLoad load() default EntityLoad.EAGER;
}
