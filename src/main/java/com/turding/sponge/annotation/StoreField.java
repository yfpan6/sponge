package com.turding.sponge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 持久化实体的字段.
 *
 * Created by yunfeng.pan on 17-6-15.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StoreField {
    /**
     * 别名.
     *
     * @return
     */
    String alias() default "";

    /**
     * 字段名.
     *
     * @return
     */
    String storeName() default "";


    /**
     * 字段是否可以被修改.
     *
     * @return
     */
    boolean updatable() default true;
}
