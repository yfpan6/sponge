package com.turding.sponge.database;

import com.turding.sponge.core.CombinedExpression;
import com.turding.sponge.core.ConditionPart;
import com.turding.sponge.core.Entity;
import com.turding.sponge.core.Expression;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 将 CombinedExpression 解析成 Sql where 查询条件.
 *
 * Created by yunfeng.pan on 17-6-19.
 */
public final class SqlConditionParser {
    private Entity entity;
    private CombinedExpression condition;
    private ParsedSqlConditionParser parsedSqlConditionParser;
    private Result result;

    private SqlConditionParser(CombinedExpression condition,
                               Entity entity) {
        this.condition = condition;
        this.entity = entity;
        this.parsedSqlConditionParser = new ParsedSqlConditionParser();
    }

    public static SqlConditionParser of(CombinedExpression condition, Entity entity) {
        return new SqlConditionParser(condition, entity);
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
    private Optional<StringBuilder> buildSql(CombinedExpression condition) {
        if (condition == null || condition.isEmpty()) {
            return Optional.empty();
        }
        StringBuilder sql = new StringBuilder();
        ConditionPart[] parts = condition.getParts();
        handleExpression(sql, parts[0].getExpression());
        sql.append(" ");
        for (int i = 1; i < parts.length; i++) {
            if (parts[i] instanceof CombinedExpression.And) {
                sql.append("and ");
            } else {
                sql.append("or ");
            }
            handleExpression(sql, parts[i].getExpression());
            sql.append(" ");
        }
        sql.setLength(sql.length() - 1);
        return Optional.of(sql);
    }

    private void handleExpression(StringBuilder sql, Expression expression) {
        if (expression instanceof Expression.Wrapper) {
            Expression.Wrapper wrapper = (Expression.Wrapper) expression;
            Optional<StringBuilder> optional = buildSql(wrapper.getWrappedCondition());
            if (optional.isPresent()) {
                sql.append("(").append(optional.get()).append(")");
            }
        } else {
            if (expression instanceof Expression.SingleValueExpression) {
                Expression.SingleValueExpression svExp = (Expression.SingleValueExpression) expression;
                String operator = getSingleValueOperator(expression);
                sql.append(entity.getFieldStoreNameByFieldName(svExp.getFieldName()))
                        .append(" ")
                        .append(operator)
                        .append(" ");
                if ("like".equals(operator)
                        || "not like".equals(operator)) {
                    if (expression instanceof Expression.StartWith) {
                        sql.append("concat(?, '%')");
                    } else if (expression instanceof Expression.EndWith) {
                        sql.append("concat('%', ?)");
                    } else {
                        sql.append("concat('%', ?, '%')");
                    }
                } else {
                    sql.append("?");
                }
                result.prepareValueList.add(svExp.getValue());
            } else {
                Expression.MuitlValueExpression mvExp = (Expression.MuitlValueExpression) expression;
                if (expression instanceof Expression.In) {
                    sql.append(entity.getFieldStoreNameByFieldName(mvExp.getFieldName())).append(" in (");
                    for (Object value: mvExp.getValues()) {
                        sql.append("?,");
                        result.prepareValueList.add(value);
                    }
                    sql.setLength(sql.length() - 1);
                    sql.append(")");
                } else if (expression instanceof Expression.NotIn) {
                    sql.append(mvExp.getFieldName())
                            .append(" not in (");
                    for (Object value: mvExp.getValues()) {
                        sql.append("?, ");
                        result.prepareValueList.add(value);
                    }
                    sql.setLength(sql.length() - 2);
                    sql.append(")");
                } else if (expression instanceof Expression.Between) {
                    sql.append(mvExp.getFieldName())
                            .append("between ? and ?");
                    result.prepareValueList.add(mvExp.getValues()[0]);
                    result.prepareValueList.add(mvExp.getValues()[1]);
                }
            }
        }
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
                return "like";
            }
            if (expression instanceof Expression.NotContain) {
                return "not like";
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
    }

    public final class ParsedSqlConditionParser {
        public Result result() {
            return result;
        }
    }
}
