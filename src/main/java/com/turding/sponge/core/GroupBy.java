package com.turding.sponge.core;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 分组表达式
 *
 * Created by yunfeng.pan on 17-6-16.
 */
@AllArgsConstructor
public class GroupBy {

    protected List<String> fieldNames;

    public List<String> fieldNames() {
        return fieldNames;
    }

    public static GroupBy of(String... fieldNames) {
        return new GroupBy(Arrays.asList(fieldNames));
    }

}
