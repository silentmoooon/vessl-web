package org.vessl.annotation;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface VesslWeb {
    String value() default "";
}
