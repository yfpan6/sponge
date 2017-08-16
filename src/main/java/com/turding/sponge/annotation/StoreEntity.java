package com.turding.sponge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 持久化实体.
 *
 * Created by yunfeng.pan on 17-6-15.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StoreEntity {
    /**
     * 实体名.
     * @return
     */
    String name() default "";

    /**
     * 存储目标.
     * <p>如果不指定, 则以实体名称为准</p>
     *
     * @return
     */
    String storeTarget() default "";
}
