package com.turding.sponge.core;

/**
 * Expression 工厂类
 * Created by yunfeng.pan on 17-8-18.
 */
public interface Exps {

    static Expression.Raw raw(String rawExp) {
        return new Expression.Raw(rawExp);
    }

    static Expression.Val val(Object value) {
        return new Expression.Val(value);
    }

    static Expression.Eq eq(String fieldName, Object value) {
        return new Expression.Eq(fieldName, value);
    }

    static Expression.UnEq unEq(String fieldName, Object value) {
        return new Expression.UnEq(fieldName, value);
    }

    static Expression.GtEq gtEq(String fieldName, Object value) {
        return new Expression.GtEq(fieldName, value);
    }

    static Expression.LtEq ltEq(String fieldName, Object value) {
        return new Expression.LtEq(fieldName, value);
    }

    static Expression.Gt gt(String fieldName, Object value) {
        return new Expression.Gt(fieldName, value);
    }

    static Expression.Lt lt(String fieldName, Object value) {
        return new Expression.Lt(fieldName, value);
    }

    static Expression.Contain contain(String fieldName, Object value) {
        return new Expression.Contain(fieldName, value);
    }

    static Expression.StartWith startWith(String fieldName, Object value) {
        return new Expression.StartWith(fieldName, value);
    }

    static Expression.EndWith endWith(String fieldName, Object value) {
        return new Expression.EndWith(fieldName, value);
    }

    static Expression.NotContain notContain(String fieldName, Object value) {
        return new Expression.NotContain(fieldName, value);
    }

    static Expression.Between between(String fieldName, Object from, Object to) {
        return new Expression.Between(fieldName, from, to);
    }

    static Expression.In in(String fieldName, Object... values) {
        return new Expression.In(fieldName, values);
    }

    static Expression.NotIn notIn(String fieldName, Object... values) {
        return new Expression.NotIn(fieldName, values);
    }

    static Expression.Not not(ComposableExpression expression) {
        return new Expression.Not(expression);
    }

    static Expression.Count count(ComposableExpression expression) {
        return new Expression.Count(expression);
    }

    static Expression.Sum sum(ComposableExpression expression) {
        return new Expression.Sum(expression);
    }

    static Expression.Avg avg(ComposableExpression expression) {
        return new Expression.Avg(expression);
    }

    static Expression.Max max(ComposableExpression expression) {
        return new Expression.Max(expression);
    }

    static Expression.Min min(ComposableExpression expression) {
        return new Expression.Min(expression);
    }

    static Expression.Distinct distinct(String fieldName) {
        return new Expression.Distinct(fieldName);
    }
}
