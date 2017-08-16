package com.turding.sponge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注非持久化字段
 *
 * Created by yunfeng.pan on 17-6-15.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
    boolean searchable() default false;
    String searchName() default "";
}