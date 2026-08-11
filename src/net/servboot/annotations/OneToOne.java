package net.servboot.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {
    Class<?> targetClass() default void.class;
}
