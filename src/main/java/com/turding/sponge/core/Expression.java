package com.turding.sponge.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 查询表达式
 *
 * Created by yunfeng.pan on 17-6-16.
 */
public interface Expression {

    static Expression raw(String rawExp) {
        return new Raw(rawExp);
    }

    static Expression eq(String fieldName, Object value) {
        return new Eq(fieldName, value);
    }

    static Expression unEq(String fieldName, Object value) {
        return new UnEq(fieldName, value);
    }

    static Expression gtEq(String fieldName, Object value) {
        return new GtEq(fieldName, value);
    }

    static Expression ltEq(String fieldName, Object value) {
        return new LtEq(fieldName, value);
    }

    static Expression gt(String fieldName, Object value) {
        return new GtEq(fieldName, value);
    }

    static Expression lt(String fieldName, Object value) {
        return new LtEq(fieldName, value);
    }

    static Expression contain(String fieldName, Object value) {
        return new Contain(fieldName, value);
    }

    static Expression startWith(String fieldName, Object value) {
        return new StartWith(fieldName, value);
    }

    static Expression endWith(String fieldName, Object value) {
        return new EndWith(fieldName, value);
    }

    static Expression notContain(String fieldName, Object value) {
        return new NotContain(fieldName, value);
    }

    static Expression between(String fieldName, Object from, Object to) {
        return new Between(fieldName, from, to);
    }

    static Expression in(String fieldName, Object... values) {
        return new In(fieldName, values);
    }

    static Expression notIn(String fieldName, Object... values) {
        return new NotIn(fieldName, values);
    }

    class Eq extends RelationalExpression {
        public Eq(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class UnEq extends RelationalExpression {
        public UnEq(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class GtEq extends RelationalExpression {
        public GtEq(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class LtEq extends RelationalExpression {
        public LtEq(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class Gt extends RelationalExpression {
        public Gt(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class Lt extends RelationalExpression {
        public Lt(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class NotNull extends Raw {
        public NotNull(String fieldName) {
            super(fieldName);
        }
    }

    class IsNull extends Raw {
        public IsNull(String fieldName) {
            super(fieldName);
        }
    }

    class Contain extends SingleValueExpression {
        public Contain(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class StartWith extends Contain {
        public StartWith(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class EndWith extends Contain {
        public EndWith(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class NotContain extends SingleValueExpression {
        public NotContain(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    class Between extends MuitlValueExpression {
        public Between(String fieldName, Object from, Object to) {
            super(fieldName, new Object[]{from, to});
        }
    }

    class In extends MuitlValueExpression {
        public In(String fieldName, Object... values) {
            super(fieldName, values);
        }
    }

    class NotIn extends MuitlValueExpression {
        public NotIn(String fieldName, Object... values) {
            super(fieldName, values);
        }
    }

    @AllArgsConstructor
    class Raw implements Expression {
        @Getter
        private String fieldName;
    }

    @AllArgsConstructor
    class SingleValueExpression implements Expression {
        @Getter
        private String fieldName;
        @Getter
        private Object value;
    }

    @AllArgsConstructor
    class MuitlValueExpression implements Expression {
        @Getter
        private String fieldName;
        @Getter
        private Object[] values;
    }

    /**
     * 关系表达式： =(等于)、<（小于）、<=（小于等于）、>（大于）、>=（大于等于）、<>（不等于）
     */
    abstract class  RelationalExpression extends SingleValueExpression {
        public RelationalExpression(String fieldName, Object value) {
            super(fieldName, value);
        }
    }

    /**
     * 逻辑表达式： NOT（非）、AND（与）、OR（或）
     */
    @AllArgsConstructor
    abstract class LogicalExpression implements Expression {
        @Getter
        private Expression expression;
    }

    class Not extends LogicalExpression {
        public Not(Expression expression) {
            super(expression);
        }
    }

    class And extends LogicalExpression{
        public And(Expression expression) {
            super(expression);
        }
    }

    class Or extends LogicalExpression {
        public Or(Expression expression) {
            super(expression);
        }
    }

    @AllArgsConstructor
    class AggregateExpression implements Expression {
        @Getter
        private Expression expression;
    }

    class Sum extends AggregateExpression {
        public Sum(Expression expression) {
            super(expression);
        }
    }

    class Avg extends AggregateExpression {
        public Avg(Expression expression) {
            super(expression);
        }
    }

    class Count extends AggregateExpression {
        public Count(Expression expression) {
            super(expression);
        }
    }

    class Max extends AggregateExpression {
        public Max(Expression expression) {
            super(expression);
        }
    }

    class Min extends AggregateExpression {
        public Min(Expression expression) {
            super(expression);
        }
    }

    class CaseWhen implements Expression {
        private Expression caseExp;
        private List<Expression> exps;

        public static When ofCaseWhen(Expression whenExp) {
            CaseWhen caseWhen = new CaseWhen();
            caseWhen.exps.add(whenExp);
            return new When(caseWhen, whenExp);
        }

        public static Case ofCase(Expression caseExp) {
            CaseWhen caseWhen = new CaseWhen();
            caseWhen.caseExp = caseExp;
            return new Case(caseWhen, caseExp);
        }

        @AllArgsConstructor
        static class Case implements Expression {
            private CaseWhen caseWhen;
            private Expression caseExp;

            public When when(Expression caseExp) {
                caseWhen.exps.add(caseExp);
                return new When(caseWhen, caseExp);
            }
        }

        @AllArgsConstructor
        static class When implements Expression {
            private CaseWhen caseWhen;
            private Expression whenExp;

            public Then then(Expression thenExp) {
                caseWhen.exps.add(thenExp);
                return new Then(caseWhen, thenExp);
            }
        }

        @AllArgsConstructor
        static class Then implements Expression {
            private CaseWhen caseWhen;
            private Expression thenExp;

            public When when(Expression whenExp) {
                caseWhen.exps.add(whenExp);
                return new When(caseWhen, whenExp);
            }

            public Else els(Expression elseExp) {
                caseWhen.exps.add(elseExp);
                return new Else(caseWhen, elseExp);
            }

            public CaseWhen end() {
                return caseWhen;
            }
        }

        @AllArgsConstructor
        static class Else implements Expression {
            private CaseWhen caseWhen;
            private Expression elseExp;
            public CaseWhen end() {
                return caseWhen;
            }
        }
    }

    @AllArgsConstructor
    class Simple implements Expression {
        @Getter
        String fieldName;
        @Getter
        String operation;
        @Getter
        Object value;
    }

    @AllArgsConstructor
    class Wrapper implements Expression {
        @Getter
        Expression wrappedExpression;
    }

}
