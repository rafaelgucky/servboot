package net.servboot.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {
    Class<?> targetClass() default void.class;
}
