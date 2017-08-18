package com.turding.sponge.core;

import com.turding.sponge.util.ObjectUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * 组合表达式
 *
 * Created by yunfeng.pan on 17-6-16.
 */
public class CombinedExpression implements ComposableExpression {

    private List<Expression> parts;

    private CombinedExpression(ComposableExpression expression) {
        parts = new LinkedList<>();
        parts.add(expression);
    }

    /**
     * 将java bean 或 map对象解析成 CombinedExpression
     *
     * @param object
     * @return
     */
    public static CombinedExpression parse(Object object) {
        // TODO 如果object是基础数据类型，则抛出异常。
        CombinedExpression condition = new CombinedExpression(null);
        if (object == null) {
            return condition;
        }

        ObjectUtil.fetchFieldAndValueKVMap(object).forEach((fieldName, value) -> {
            if (value != null) {
                condition.and(Exps.eq(fieldName, value));
            }
        });
        return condition;
    }

    public static CombinedExpression of(ComposableExpression expression) {
        return new CombinedExpression(expression);
    }

    public Expression[] getParts() {
        return parts.toArray(new Expression[0]);
    }

    public boolean isEmpty() {
        return parts.size() == 0;
    }

    public CombinedExpression and(ComposableExpression expression) {
        parts.add(new Expression.And(expression));
        return this;
    }

    public CombinedExpression or(ComposableExpression expression) {
        parts.add(new Expression.Or(expression));
        return this;
    }

    @Deprecated
    public CombinedExpression and(String fieldName, String operation, Object value) {
        parts.add(new Expression.And(new Expression.Simple(fieldName, operation, value)));
        return this;
    }

    @Deprecated
    public CombinedExpression or(String fieldName, String operation, Object value) {
        parts.add(new Expression.Or(new Expression.Simple(fieldName, operation, value)));
        return this;
    }

    public Wrapper wrapUp() {
        return new Expression.Wrapper(this);
    }
}
