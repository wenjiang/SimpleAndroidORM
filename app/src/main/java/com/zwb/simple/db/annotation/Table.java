package com.zwb.simple.db.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by pc on 2015/3/4.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
    String table() default "";
}
