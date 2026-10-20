package net.servboot.annotations;

import net.servboot.annotations.enums.EntityLoad;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";
    boolean notNull() default false;
    int length() default 255;
    EntityLoad load() default EntityLoad.EAGER;
}
