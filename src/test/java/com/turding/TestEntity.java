package com.turding;

import com.turding.sponge.annotation.PK;
import com.turding.sponge.annotation.StoreEntity;
import com.turding.sponge.annotation.StoreField;
import com.turding.sponge.annotation.Transient;
import com.turding.sponge.core.Storable;

/**
 * Created by yunfeng.pan on 17-8-17.
 */
@StoreEntity(storeTarget = "test_entity")
public class TestEntity implements Storable {

    @PK
    private Integer id;

    @StoreField
    private String name;

    @StoreField
    private String sex;

    @StoreField
    private int age;

    @Transient
    private int count;

    @Transient
    private int sum;
}
