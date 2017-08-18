package com.turding.sponge.database;

import com.turding.sponge.core.*;
import com.turding.sponge.util.StringUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
        this.parseEntityClass()
            .parseSelectFields()
            .parseCondition()
            .parseOrderBy()
            .parsePagination();
        return parsedSqlQueryStructureParser;
    }

    private SqlQueryStructureParser parseEntityClass() {
        if (!validParser) {
            return this;
        }
        entity = EntityParser.of(parseTarget.entityType()).parse().result();
        result.tableName = entity.getStoreTarget();
        return this;
    }

    private SqlQueryStructureParser parseSelectFields() {
        if (!validParser) {
            return this;
        }

        Optional<QueryField[]> selectFields = parseTarget.queryFields();
        if (selectFields.isPresent()) {
            result.selectFieldList = SqlSelectFieldsParser.of(entity, selectFields.get())
                    .parse().result().getEntityFieldList();
        } else {
            result.selectFieldList = entity.getFieldList();
            result.selectFieldList = result.selectFieldList.stream()
                    .filter(field -> field.isSearchable())
                    .collect(Collectors.toList());
        }
        return this;
    }

    private SqlQueryStructureParser parseCondition() {
        if (!validParser) {
            return this;
        }
        Optional<CombinedExpression> condition = parseTarget.filterCondition();
        if (condition.isPresent()) {
            SqlExpressionParser.Result scpResult = SqlExpressionParser.of(condition.get(), entity)
                    .parse().result();
            result.wherePrepareSql = scpResult.getPrepareSql();
            result.wherePrepareValueList = scpResult.getPrepareValueList();
        }
        return this;
    }

    private SqlQueryStructureParser parseOrderBy() {
        if (!validParser) {
            return this;
        }
        Optional<OrderBy[]> orderByList = parseTarget.orders();
        if (orderByList.isPresent()) {
            result.orderBySql = SqlOrderByParser.of(orderByList.get(), entity)
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
        private List<Entity.Field> selectFieldList;
        private String wherePrepareSql;
        private List<Object> wherePrepareValueList;
        private String orderBySql;
        private String paginationPrepareSql;
        private Object[] paginationPrepareValues;

        private String prepareSql;

        private Object[] prepareValues;

        public String prepareSql() {
            return prepareSql;
        }

        public Object[] prepareValues() {
            return prepareValues;
        }

        public List<Entity.Field> selectFieldList() {
            return selectFieldList;
        }

        public String sql() {
            int startPos = prepareSql.indexOf('?');
            if (startPos == -1) {
                return prepareSql;
            }

            StringBuilder sql = new StringBuilder();
            char c;
            Object value;
            for (int i = 0, pos = 0, len = prepareSql.length(); i < len; i++) {
                c = prepareSql.charAt(i);
                if ('?' == c) {
                    value = prepareValues[pos];
                    pos++;
                    if (value == null
                            || value instanceof Integer
                            || value instanceof Double
                            || value instanceof Long
                            || value instanceof Byte
                            || value instanceof Float
                            || value instanceof Short) {
                        sql.append(value);
                        continue;
                    }
                    sql.append('\'');
                    if (value instanceof Date) {
                        sql.append(value);
                    } else if (value instanceof LocalDate
                            || value instanceof LocalDateTime) {
                        sql.append(value);
                    } else {
                        sql.append(value);
                    }
                    sql.append('\'');
                } else {
                    sql.append(c);
                }
            }
            return sql.toString();
        }

        private void buildPrepareSql() {
            StringBuilder prepareSql = new StringBuilder();
            prepareSql.append("SELECT ");
            selectFieldList.forEach(field -> {
                prepareSql.append(field.getStoreName()).append(", ");
            });
            prepareSql.setLength(prepareSql.length() - 2);
            prepareSql.append(" FROM ").append(tableName);
            if (!StringUtil.isBlank(wherePrepareSql)) {
                prepareSql.append(" WHERE ").append(wherePrepareSql);
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
            if (wherePrepareValueList != null) {
                pvs.addAll(wherePrepareValueList);
            }
            if (paginationPrepareValues != null) {
                pvs.add(paginationPrepareValues[0]);
                pvs.add(paginationPrepareValues[1]);
            }
            prepareValues = pvs.toArray();
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
