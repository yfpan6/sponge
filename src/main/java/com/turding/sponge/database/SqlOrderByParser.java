package com.turding.sponge.database;

import com.turding.sponge.core.Entity;
import com.turding.sponge.core.OrderBy;
import lombok.Getter;

/**
 * Created by yunfeng.pan on 17-6-19.
 */
public final class SqlOrderByParser {
    private Entity entity;
    private Result result;
    private OrderBy[] orderBys;
    private ParsedSqlOrderByParser parsedSqlOrderByParser;

    private SqlOrderByParser(Entity entity, OrderBy[] orderBys) {
        this.entity = entity;
        this.orderBys = orderBys;
        this.parsedSqlOrderByParser = new ParsedSqlOrderByParser();
    }

    public static SqlOrderByParser of(Entity entity, OrderBy[] orderBys) {
        return new SqlOrderByParser(entity, orderBys);
    }

    public ParsedSqlOrderByParser parse() {
        StringBuilder orderBySql = new StringBuilder();
        orderBySql.append("ORDER BY ");
        for (OrderBy orderBy : orderBys) {
            orderBySql.append(entity.getFieldStoreNameByFieldName(orderBy.fieldName()));
            if (orderBy instanceof OrderBy.Asc) {
                orderBySql.append(" ASC, ");
            } else {
                orderBySql.append(" DESC, ");
            }
        }
        orderBySql.setLength(orderBySql.length() - 2);
        result = new Result();
        result.orderBySql = orderBySql.toString();
        return parsedSqlOrderByParser;
    }

    public static final class Result {
        @Getter
        private String orderBySql;
    }

    public final class ParsedSqlOrderByParser {

        public Result result() {
            return result;
        }

    }
}
