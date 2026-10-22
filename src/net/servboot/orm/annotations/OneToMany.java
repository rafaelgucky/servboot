package net.servboot.orm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    Class<?> targetClass();
    KeyMap[] keys() default {};
}
