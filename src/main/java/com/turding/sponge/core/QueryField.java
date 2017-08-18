package com.turding.sponge.core;

import lombok.Getter;

/**
 * 查询字段
 * Created by myron on 17-8-16.
 */
public class QueryField {

    /**
     * 查询字段或查询表达式别名.
     * <p>查询字段的别名对应 Entity.Field.fieldName.</p>
     **/
    @Getter
    private String alias;

    /** 查询表达式 */
    @Getter
    private ComposableExpression fieldExp;

    private QueryField() {
    }

    /**
     * 根据字段表名生成QueryField
     *
     * @param fieldName
     * @return
     */
    public static QueryField of(String fieldName) {
        QueryField queryField = new QueryField();
        queryField.fieldExp = Exps.raw(fieldName);
        return queryField;
    }

    public static QueryField of(ComposableExpression fieldExp) {
        QueryField queryField = new QueryField();
        queryField.fieldExp = fieldExp;
        return queryField;
    }

    public static QueryField of(ComposableExpression fieldExp, String alias) {
        QueryField queryField = new QueryField();
        queryField.fieldExp = fieldExp;
        queryField.alias = alias;
        return queryField;
    }

    public static QueryField of(String fieldName, String alias) {
        QueryField queryField = new QueryField();
        queryField.fieldExp = Exps.raw(fieldName);
        queryField.alias = alias;
        return queryField;
    }

}
