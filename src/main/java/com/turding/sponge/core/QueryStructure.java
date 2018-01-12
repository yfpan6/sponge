package com.turding.sponge.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 查询结构， 用作查询入参
 *
 * Created by yunfeng.pan on 17-6-16.
 */
public class QueryStructure {

    protected Entity<?> entity;
    protected List<QueryField> fields;
    protected ComposableExpression condition;
    protected GroupBy groupBy;
    protected List<OrderBy> orderBys;
    protected Integer limit;
    protected Integer offset;

    private <T extends Storable> QueryStructure(Entity<T> entity) {
        this.entity = entity;
    }

    public static <T extends Storable> QueryStructure of(Class<T> clazz) {
        QueryStructure queryStructure = new QueryStructure(EntityParser.of(clazz).parse().result());
        queryStructure.orderBys = new ArrayList<>();
        queryStructure.fields = new ArrayList<>();
        return queryStructure;
    }

    public static <T extends Storable> QueryStructure of(Entity<T> entity) {
        QueryStructure queryStructure = new QueryStructure(entity);
        queryStructure.orderBys = new ArrayList<>();
        queryStructure.fields = new ArrayList<>();
        return queryStructure;
    }

    public QueryStructure setQueryFields(String... fieldNames) {
        fields = new ArrayList<>();
        for (int i = 0; i < fieldNames.length; i++) {
            fields.add(QueryField.of(fieldNames[i]));
        }
        return this;
    }

    public QueryStructure addQueryField(String fieldName) {
        if (fieldName == null) {
            throw new NullPointerException("param fieldName is null");
        }
        fields.add(QueryField.of(fieldName));
        return this;
    }

    public QueryStructure setQueryFields(QueryField... queryFields) {
        fields = Arrays.asList(queryFields);
        return this;
    }

    public QueryStructure addQueryField(QueryField queryField) {
        if (queryField == null) {
            throw new NullPointerException("param queryField is null");
        }
        fields.add(queryField);
        return this;
    }

    public QueryStructure filterExp(ComposableExpression condition) {
        this.condition = condition;
        return this;
    }

    public QueryStructure pagination(int pageNumber, int pageSize) {
        this.offset = (pageNumber - 1) * pageSize;
        this.limit = pageSize;
        return this;
    }

    public QueryStructure orderBy(OrderBy... orderBys) {
        this.orderBys.addAll(Arrays.asList(orderBys));
        return this;
    }

    public QueryStructure groupBy(String... groupByFields) {
        groupBy = GroupBy.of(groupByFields);
        return this;
    }

    public QueryStructure offset(int offset) {
        this.offset = offset;
        return this;
    }

    public QueryStructure limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Entity entity() {
        return entity;
    }

    public Optional<QueryField[]> setQueryFields() {
        return Optional.ofNullable(fields == null || fields.size() == 0
                ? null : fields.toArray(new QueryField[0]));
    }

    public Optional<ComposableExpression> filterExp() {
        return Optional.ofNullable(condition);
    }

    public Optional<OrderBy[]> order() {
        if (orderBys.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(orderBys.toArray(new OrderBy[0]));
    }

    public Optional<GroupBy> groupBy() {
        if (groupBy == null || groupBy.fieldNames.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(groupBy);
    }

    public Optional<Integer> limit() {
        return Optional.ofNullable(limit);
    }

    public Optional<Integer> offset() {
        return Optional.ofNullable(offset);
    }

}
