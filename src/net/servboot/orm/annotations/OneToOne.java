package net.servboot.orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {
    Class<?> targetClass() default void.class;
    KeyMap[] keys() default {};
}
