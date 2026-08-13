package net.servboot.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Key {
    String value() default "";
    boolean increment() default true;
    String[] foreign() default {};
}
