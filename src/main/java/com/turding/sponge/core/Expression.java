package com.turding.sponge.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询表达式
 *
 * Created by yunfeng.pan on 17-6-16.
 */
public interface Expression {

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
    class Raw implements ComposableExpression {
        @Getter
        private String rawExpression;
    }

    /**
     * 值
     */
    @AllArgsConstructor
    class Val implements ComposableExpression {
        @Getter
        private Object value;
    }

    /**
     * 单值表达式
     */
    @AllArgsConstructor
    abstract class SingleValueExpression implements ComposableExpression {
        @Getter
        private String fieldName;
        @Getter
        private Object value;
    }

    /**
     * 多值表达式
     */
    @AllArgsConstructor
    abstract class MuitlValueExpression implements ComposableExpression {
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

    @AllArgsConstructor
    abstract class OuterExpression {
        @Getter
        private Expression expression;
    }

    class Not extends OuterExpression implements ComposableExpression {
        public Not(ComposableExpression expression) {
            super(expression);
        }
    }

    class And  extends OuterExpression implements Expression {
        public And(ComposableExpression expression) {
            super(expression);
        }
    }

    class Or  extends OuterExpression implements Expression {
        public Or(ComposableExpression expression) {
            super(expression);
        }
    }

    /**
     * 聚合表达式
     */
    abstract class AggregateExpression extends OuterExpression implements ComposableExpression {
        public AggregateExpression(ComposableExpression expression) {
            super(expression);
        }
    }

    class Count extends AggregateExpression {
        public Count(ComposableExpression expression) {
            super(expression);
        }
    }

    class Sum extends AggregateExpression {
        public Sum(ComposableExpression expression) {
            super(expression);
        }
    }

    class Avg extends AggregateExpression {
        public Avg(ComposableExpression expression) {
            super(expression);
        }
    }

    class Max extends AggregateExpression {
        public Max(ComposableExpression expression) {
            super(expression);
        }
    }

    class Min extends AggregateExpression {
        public Min(ComposableExpression expression) {
            super(expression);
        }
    }

    @AllArgsConstructor
    class Distinct implements ComposableExpression {
        @Getter
        String fieldName;
    }
    /**
     * case when 表达式.
     */
    class CaseWhen implements ComposableExpression {
        @Getter
        private Expression caseExp;
        @Getter
        private List<Expression> exps;

        public CaseWhen(ComposableExpression caseExp) {
            this.caseExp = caseExp;
            exps = new ArrayList<>();
        }

        public static When ofCaseWhen(ComposableExpression whenExp) {
            CaseWhen caseWhen = new CaseWhen(null);
            return new When(caseWhen, whenExp);
        }

        public static Case ofCase(ComposableExpression caseExp) {
            CaseWhen caseWhen = new CaseWhen(caseExp);
            return new Case(caseWhen);
        }

        @AllArgsConstructor
        public static class Case {
            private CaseWhen caseWhen;

            public When when(ComposableExpression caseExp) {
                return new When(caseWhen, caseExp);
            }
        }

        public static class When extends OuterExpression implements Expression {
            private CaseWhen caseWhen;

            public When(CaseWhen caseWhen, Expression whenExp) {
                super(whenExp);
                this.caseWhen = caseWhen;
                caseWhen.exps.add(this);
            }

            public Then then(ComposableExpression thenExp) {
                return new Then(caseWhen, thenExp);
            }
        }

        public static class Then extends OuterExpression implements Expression {
            private CaseWhen caseWhen;

            public Then(CaseWhen caseWhen, Expression thenExp) {
                super(thenExp);
                this.caseWhen = caseWhen;
                caseWhen.exps.add(this);
            }

            public When when(ComposableExpression whenExp) {
                return new When(caseWhen, whenExp);
            }

            public Else els(ComposableExpression elseExp) {
                return new Else(caseWhen, elseExp);
            }

            public CaseWhen end() {
                return caseWhen;
            }
        }

        public static class Else extends OuterExpression implements Expression {
            private CaseWhen caseWhen;

            public Else(CaseWhen caseWhen, Expression elseExp) {
                super(elseExp);
                this.caseWhen = caseWhen;
                caseWhen.exps.add(this);
            }

            public CaseWhen end() {
                return caseWhen;
            }
        }
    }

    /**
     * 简单表达式， 可指定操作符
     */
    @AllArgsConstructor
    class Simple implements ComposableExpression, Expression {
        @Getter
        String fieldName;
        @Getter
        String operation;
        @Getter
        Object value;
    }

    /**
     * 包裹表达式.
     * 表达式 i=1
     * SQL： 包裹后 (i = 1)
     */
    @AllArgsConstructor
    class Wrapper implements ComposableExpression {
        @Getter
        ComposableExpression wrappedExpression;
    }

}
