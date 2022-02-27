package org.vessl.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpMethod {

    String value() default "";

    RequestMethod[] method() default {};

}
