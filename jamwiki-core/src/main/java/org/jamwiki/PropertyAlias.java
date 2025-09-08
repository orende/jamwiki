package org.jamwiki;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface PropertyAlias {
    String value() default "";
}
