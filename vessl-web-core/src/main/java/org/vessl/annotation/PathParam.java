package org.vessl.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathParam {
    String value() default "";
    boolean required() default true;
}
