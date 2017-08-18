package com.turding.sponge.database;

import com.turding.sponge.core.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 将 CombinedExpression 解析成 Sql where 查询条件.
 *
 * Created by yunfeng.pan on 17-6-19.
 */
public final class SqlExpressionParser {
    private Entity entity;
    private Expression condition;
    private ParsedSqlConditionParser parsedSqlConditionParser;
    private Result result;

    private SqlExpressionParser(Expression condition,
                                Entity entity) {
        this.condition = condition;
        this.entity = entity;
        this.parsedSqlConditionParser = new ParsedSqlConditionParser();
    }

    public static SqlExpressionParser of(Expression condition, Entity entity) {
        return new SqlExpressionParser(condition, entity);
    }

    public ParsedSqlConditionParser parse() {
        result = new Result();
        Optional<StringBuilder> sql = buildSql(condition);
        if (sql.isPresent()) {
            result.prepareSql = sql.get().toString();
        }
        return parsedSqlConditionParser;
    }

    /**
     * 构建Sql查询条件.
     *
     * @param condition
     * @return
     */
    private Optional<StringBuilder> buildSql(Expression condition) {
        if (condition == null) {
            return Optional.empty();
        }
        StringBuilder sql = new StringBuilder();
        handleExpression(sql, condition);
        return Optional.of(sql);
    }

    private void handleExpression(StringBuilder sql, Expression expression) {
        if (expression instanceof Expression.Raw) {
            sql.append(((Expression.Raw) expression).getRawExpression());
        } else if (expression instanceof Expression.Val) {
            sql.append('?');
            result.prepareValueList.add(((Expression.Val) expression).getValue());
        } else if (expression instanceof Expression.Wrapper) {
            Expression.Wrapper wrapper = (Expression.Wrapper) expression;
            Optional<StringBuilder> optional = buildSql(wrapper.getWrappedExpression());
            if (optional.isPresent()) {
                sql.append('(').append(optional.get()).append(')');
            }
        } else if (expression instanceof CombinedExpression) {
            Expression[] parts = ((CombinedExpression) expression).getParts();
            // 第一个表达式肯定不会是 and 或 or
            handleExpression(sql, parts[0]);
            sql.append(' ');
            for (int i = 1; i < parts.length; i++) {
                if (parts[i] instanceof ComposableExpression) {
                    handleExpression(sql, parts[i]);
                } else {
                    if (parts[i] instanceof Expression.And) {
                        sql.append("AND ");
                    } else {
                        sql.append("OR ");
                    }
                    handleExpression(sql, ((Expression.OuterExpression) parts[i]).getExpression());
                }
                sql.append(' ');
            }
            sql.setLength(sql.length() - 1);
        } else if (expression instanceof Expression.Not) {
            Optional<StringBuilder> optional = buildSql(((Expression.Not) expression).getExpression());
            if (optional.isPresent()) {
                sql.append("!(").append(optional.get()).append(')');
            }
        } else if (expression instanceof Expression.SingleValueExpression) {
            Expression.SingleValueExpression svExp = (Expression.SingleValueExpression) expression;
            String operator = getSingleValueOperator(expression);
            sql.append(entity.getFieldStoreNameByFieldName(svExp.getFieldName()))
                    .append(' ')
                    .append(operator)
                    .append(' ');
            if ("LIKE".equals(operator)
                    || "NOT LIKE".equals(operator)) {
                if (expression instanceof Expression.StartWith) {
                    sql.append("CONCAT(?, '%')");
                } else if (expression instanceof Expression.EndWith) {
                    sql.append("CONCAT('%', ?)");
                } else {
                    sql.append("CONCAT('%', ?, '%')");
                }
            } else {
                sql.append('?');
            }
            result.prepareValueList.add(svExp.getValue());
        } else  if (expression instanceof Expression.MuitlValueExpression) {
            Expression.MuitlValueExpression mvExp = (Expression.MuitlValueExpression) expression;
            if (expression instanceof Expression.In) {
                sql.append(entity.getFieldStoreNameByFieldName(mvExp.getFieldName())).append(" IN (");
                for (Object value: mvExp.getValues()) {
                    sql.append("?,");
                    result.prepareValueList.add(value);
                }
                sql.setLength(sql.length() - 1);
                sql.append(')');
            } else if (expression instanceof Expression.NotIn) {
                sql.append(mvExp.getFieldName())
                        .append(" NOT IN (");
                for (Object value: mvExp.getValues()) {
                    sql.append("?, ");
                    result.prepareValueList.add(value);
                }
                sql.setLength(sql.length() - 2);
                sql.append(')');
            } else if (expression instanceof Expression.Between) {
                sql.append(mvExp.getFieldName())
                        .append("BETWEEN ? AND ?");
                result.prepareValueList.add(mvExp.getValues()[0]);
                result.prepareValueList.add(mvExp.getValues()[1]);
            }
        } else if (expression instanceof Expression.CaseWhen) {
            handleCaseWhenExpression(sql, (Expression.CaseWhen) expression);
        } else if (expression instanceof Expression.AggregateExpression) {
            handleAggregateExpression(sql, (Expression.AggregateExpression) expression);
        } else {
            throw new IllegalArgumentException("unsupported expression: " + expression.getClass());
        }
    }

    private void handleCaseWhenExpression(StringBuilder sql, Expression.CaseWhen caseWhen) {
        Expression caseExp = caseWhen.getCaseExp();
        sql.append("CASE");
        if (caseExp != null) {
            sql.append(' ');
            handleExpression(sql, caseExp);
        }
        sql.append(' ');
        for (Expression exp: caseWhen.getExps()) {
            if (exp instanceof Expression.CaseWhen.When) {
                sql.append("WHEN ");
            } else if (exp instanceof Expression.CaseWhen.Then) {
                sql.append("THEN ");
            } else if (exp instanceof Expression.CaseWhen.Else) {
                sql.append("ELSE ");
            }
            handleExpression(sql, ((Expression.OuterExpression) exp).getExpression());
            sql.append(' ');
        }
        sql.append("END");
    }

    /**
     * 聚合表达式
     */
    private void handleAggregateExpression(StringBuilder sql, Expression.AggregateExpression expression) {
        if (expression instanceof Expression.Count) {
            sql.append("COUNT");
        } else if (expression instanceof Expression.Sum) {
            sql.append("SUM");
        } else if (expression instanceof Expression.Avg) {
            sql.append("AVG");
        } else if (expression instanceof Expression.Max) {
            sql.append("MAX");
        } else if (expression instanceof Expression.Min) {
            sql.append("MIN");
        }
        sql.append('(');
        handleExpression(sql, expression.getExpression());
        sql.append(')');
    }

    /**
     * 获取表达式操作符.
     *
     * @param expression
     * @return
     */
    private String getSingleValueOperator(Expression expression) {
        if (expression != null) {
            if (expression instanceof Expression.Eq) {
                return "=";
            }
            if (expression instanceof Expression.UnEq) {
                return "!=";
            }
            if (expression instanceof Expression.GtEq) {
                return ">=";
            }
            if (expression instanceof Expression.LtEq) {
                return "<=";
            }
            if (expression instanceof Expression.Gt) {
                return ">";
            }
            if (expression instanceof Expression.Lt) {
                return "<";
            }
            if (expression instanceof Expression.Contain) {
                return "LIKE";
            }
            if (expression instanceof Expression.NotContain) {
                return "NOT LIKE";
            }
        }

        throw new IllegalArgumentException("expression undefined.");
    }

    public static final class Result {
        /**
         * prepareSql 的参数值列表.
         */
        @Getter
        private List<Object> prepareValueList;
        /**
         * prepareSql.
         */
        @Getter
        private String prepareSql;

        private Result() {
            this.prepareValueList = new ArrayList<>();
        }

        /**
         * 获取替换后的原始SQL
         * @return
         */
        public String getRawSql() {
            return SqlUtil.toRawSql(prepareSql, prepareValueList);
        }
    }

    public final class ParsedSqlConditionParser {
        public Result result() {
            return result;
        }
    }
}
