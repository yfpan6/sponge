package com.turding.sponge.core;

import com.turding.sponge.util.ObjectUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 组合表达式
 *
 * Created by yunfeng.pan on 17-6-16.
 */
public class CombinedExpression implements ComposableExpression {

    private List<Expression> parts = new LinkedList<>();

    private CombinedExpression(ComposableExpression expression) {
        assertNull(expression);
        parts.add(expression);
    }

    /**
     * 将java bean 或 map对象解析成 CombinedExpression
     *
     * @param object
     * @return
     */
    public static <T extends Storable>  CombinedExpression parse(T object) {
        if (object == null) {
            throw new NullPointerException("the param is null.");
        }

        List<Entity.Field> fields = EntityParser.of(object)
                .parse().result().getSearchableFields();
        CombinedExpression condition = null;
        boolean first = true;
        for (Entity.Field field : fields) {
            if (first) {
                condition = new CombinedExpression(Exps.eq(field.getFieldName(), field.getValue()));
                first = false;
            } else {
                condition.and(Exps.eq(field.getFieldName(), field.getValue()));
            }
        }

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
        assertNull(expression);
        parts.add(new Expression.And(expression));
        return this;
    }

    public CombinedExpression or(ComposableExpression expression) {
        assertNull(expression);
        parts.add(new Expression.Or(expression));
        return this;
    }

    @Deprecated
    public CombinedExpression and(String fieldName, String operation, Object value) {
        assertNull(fieldName);
        parts.add(new Expression.And(new Expression.Simple(fieldName, operation, value)));
        return this;
    }

    @Deprecated
    public CombinedExpression or(String fieldName, String operation, Object value) {
        assertNull(fieldName);
        parts.add(new Expression.Or(new Expression.Simple(fieldName, operation, value)));
        return this;
    }

    public Wrapper wrapUp() {
        return new Expression.Wrapper(this);
    }

    private void assertNull(Object param) {
        if (param == null) {
            throw new NullPointerException("the param is null.");
        }
    }
}
