package org.vessl.webstarter;

import org.springframework.context.annotation.Import;
import org.vessl.bind.WebPackageScan;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({VesslAutoConfiguration.class,WebPackageScan.class})
public @interface EnableVesslWeb {
    String[] value() default {};
}
