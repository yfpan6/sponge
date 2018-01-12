package com.turding.sponge.database;

import com.turding.sponge.core.*;
import com.turding.sponge.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于DB的查询结构解析器
 *
 * Created by yunfeng.pan on 17-6-16.
 */
public final class SqlQueryStructureParser {

    private QueryStructure parseTarget;
    private boolean validParser = true;

    private Entity entity;
    private Result result;
    private ParsedSqlQueryStructureParser parsedSqlQueryStructureParser;

    private SqlQueryStructureParser(QueryStructure parseTarget) {
        this.parseTarget = parseTarget;
        this.validParser = parseTarget != null;
        this.parsedSqlQueryStructureParser = new ParsedSqlQueryStructureParser();
    }

    public static SqlQueryStructureParser of(QueryStructure queryStructure) {
        return new SqlQueryStructureParser(queryStructure);
    }

    public ParsedSqlQueryStructureParser parse() {
        result = new Result();
        this.parseEntity()
            .parseSelectFields()
            .parseCondition()
            .parseGroupBy()
            .parseOrderBy()
            .parsePagination();
        return parsedSqlQueryStructureParser;
    }

    private SqlQueryStructureParser parseEntity() {
        if (!validParser) {
            return this;
        }

        entity = parseTarget.entity();
        result.tableName = entity.getStoreTarget();
        return this;
    }

    private SqlQueryStructureParser parseSelectFields() {
        if (!validParser) {
            return this;
        }

        Optional<QueryField[]> queryFields = parseTarget.setQueryFields();
        if (queryFields.isPresent()) {
            SqlQueryFieldsParser.Result parserdResult = SqlQueryFieldsParser.of(entity,queryFields.get())
                    .parse().result();
            result.selectFields = parserdResult.getSelectFields();
            result.entityFields = parserdResult.getEntityFields();
        } else {
            List<Entity.Field> entityFields = entity.getFields();
            result.selectFields = entityFields.stream()
                    .filter(field -> field.isSearchable())
                    .map(Entity.Field::getStoreName)
                    .collect(Collectors.toList());
            result.entityFields = entityFields.stream()
                    .filter(field -> field.isSearchable())
                    .collect(Collectors.toList());
        }
        return this;
    }

    private SqlQueryStructureParser parseCondition() {
        if (!validParser) {
            return this;
        }
        Optional<ComposableExpression> condition = parseTarget.filterExp();
        if (condition.isPresent()) {
            SqlExpressionParser.Result scpResult = SqlExpressionParser.of(entity, condition.get())
                    .parse().result();
            result.wherePrepareSql = scpResult.getPrepareSql();
            result.wherePrepareValues = scpResult.getPrepareValues();
        }
        return this;
    }

    private SqlQueryStructureParser parseGroupBy() {
        if (!validParser) {
            return this;
        }

        Optional<GroupBy> groupBy = parseTarget.groupBy();
        if (groupBy.isPresent()) {
            result.groupBySql = SqlGroupByParser.of(entity, groupBy.get())
                    .parse().result().getGroupBySql();
        }
        return this;
    }

    private SqlQueryStructureParser parseOrderBy() {
        if (!validParser) {
            return this;
        }
        Optional<OrderBy[]> orderByList = parseTarget.order();
        if (orderByList.isPresent()) {
            result.orderBySql = SqlOrderByParser.of(entity, orderByList.get())
                    .parse()
                    .result()
                    .getOrderBySql();
        }
        return this;
    }

    private SqlQueryStructureParser parsePagination() {
        if (!validParser) {
            return this;
        }
        Optional<Integer> offset = parseTarget.offset();
        if (!offset.isPresent()) {
            return this;
        }
        Optional<Integer> limit = parseTarget.limit();
        if (!limit.isPresent()) {
            return this;
        }

        result.paginationPrepareSql = "limit ?, ?";
        result.paginationPrepareValues = new Object[]{offset.get(), limit.get()};
        return this;

    }

    public static final class Result {

        private String tableName;
        private List<String> selectFields;
        private List<Entity.Field> entityFields;
        private String wherePrepareSql;
        private List<Object> wherePrepareValues;
        private String groupBySql;
        private String orderBySql;
        private String paginationPrepareSql;
        private Object[] paginationPrepareValues;
        private String prepareSql;
        private List<Object> prepareValues;

        public String prepareSql() {
            return prepareSql;
        }

        public List<Object> prepareValues() {
            return prepareValues;
        }

        public List<String> selectFields() {
            return selectFields;
        }

        public List<Entity.Field> entityFields() {
            return entityFields;
        }

        public String rawSql() {
            return SqlUtil.toRawSql(prepareSql, prepareValues);
        }

        private void buildPrepareSql() {
            StringBuilder prepareSql = new StringBuilder();
            prepareSql.append("SELECT ");
            selectFields.forEach(selectField -> {
                prepareSql.append(selectField).append(", ");
            });
            prepareSql.setLength(prepareSql.length() - 2);
            prepareSql.append(" FROM ").append(tableName);
            if (!StringUtil.isBlank(wherePrepareSql)) {
                prepareSql.append(" WHERE ").append(wherePrepareSql);
            }
            if (!StringUtil.isBlank(groupBySql)) {
                prepareSql.append(' ').append(groupBySql);
            }
            if (!StringUtil.isBlank(orderBySql)) {
                prepareSql.append(' ').append(orderBySql);
            }
            if (!StringUtil.isBlank(paginationPrepareSql)) {
                prepareSql.append(' ').append(paginationPrepareSql);
            }

            this.prepareSql =  prepareSql.toString();
        }

        private void buildPrepareValues() {
            List<Object> pvs = new ArrayList<>();
            if (wherePrepareValues != null) {
                pvs.addAll(wherePrepareValues);
            }
            if (paginationPrepareValues != null) {
                pvs.add(paginationPrepareValues[0]);
                pvs.add(paginationPrepareValues[1]);
            }
            prepareValues = pvs;
        }
    }

    public final class ParsedSqlQueryStructureParser {
        public Result result() {
            result.buildPrepareSql();
            result.buildPrepareValues();
            return result;
        }
    }
}
