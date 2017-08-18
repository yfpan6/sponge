package com.turding.sponge.core;

import lombok.Getter;
import lombok.Setter;

/**
 * 查询字段
 * Created by myron on 17-8-16.
 */
public class QueryField {

    /** 表达式别名 */
    @Getter
    @Setter
    private String fieldName;

    /** 查询表达式 */
    @Getter
    @Setter
    private ComposableExpression storeFieldExp;

    private QueryField(String fieldName) {
        this.fieldName = fieldName;
    }

    private QueryField(ComposableExpression storeFieldExp) {
        this.storeFieldExp = storeFieldExp;
    }

    private QueryField(String fieldName, ComposableExpression storeFieldExp) {
        this.fieldName = fieldName;
        this.storeFieldExp = storeFieldExp;
    }

    public static QueryField of(String fieldName) {
        return new QueryField(fieldName);
    }

    public static QueryField of(ComposableExpression storeFieldExp) {
        return new QueryField(storeFieldExp);
    }

    public static QueryField of(String fieldName, ComposableExpression storeFieldExp) {
        return new QueryField(fieldName, storeFieldExp);
    }

}
