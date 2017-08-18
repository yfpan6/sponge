package com.turding.sponge.database;

import com.turding.sponge.core.Entity;
import com.turding.sponge.core.GroupBy;
import lombok.Getter;

/**
 * Created by yunfeng.pan on 17-6-19.
 */
public final class SqlGroupByParser {
    private Entity entity;
    private Result result;
    private GroupBy groupBy;
    private ParsedSqlGroupByParser parsedSqlGroupByParser;

    private SqlGroupByParser(Entity entity, GroupBy groupBy) {
        this.entity = entity;
        this.groupBy = groupBy;
        this.parsedSqlGroupByParser = new ParsedSqlGroupByParser();
    }

    public static SqlGroupByParser of(Entity entity, GroupBy groupBy) {
        return new SqlGroupByParser(entity, groupBy);
    }

    public ParsedSqlGroupByParser parse() {
        StringBuilder groupBySql = new StringBuilder();
        groupBySql.append("GROUP BY ");
        for (String fieldName : groupBy.fieldNames()) {
            groupBySql.append(entity.getFieldStoreNameByFieldName(fieldName));
            groupBySql.append(", ");
        }
        groupBySql.setLength(groupBySql.length() - 2);
        result = new Result();
        result.groupBySql = groupBySql.toString();
        return parsedSqlGroupByParser;
    }

    public static final class Result {
        @Getter
        private String groupBySql;
    }

    public final class ParsedSqlGroupByParser {
        public Result result() {
            return result;
        }
    }
}
