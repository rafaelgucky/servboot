package net.servboot.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
    Class<?> entity();
    boolean notNull() default false;
}
