package com.turding.sponge.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 查询结构， 用作查询入参
 *
 * Created by yunfeng.pan on 17-6-16.
 */
public class QueryStructure<T extends Storable> {

    protected Class<T> entityType;
    protected String[] fields;
    protected CombinedExpression condition;
    protected List<OrderBy> orderByList;
    protected GroupBy groupBy;
    protected Integer limit;
    protected Integer offset;

    private QueryStructure(Class<T> entityType) {
        this.entityType = entityType;
    }

    public static  <T extends Storable> QueryStructure<T> of(Class<T> clazz) {
        QueryStructure queryStructure = new QueryStructure(clazz);
        queryStructure.orderByList = new ArrayList<>();
        return queryStructure;
    }

    public QueryStructure<T> setQueryFields(String... fieldNames) {
        fields = fieldNames;
        return this;
    }

    public QueryStructure<T> setFilterCondition(CombinedExpression condition) {
        this.condition = condition;
        return this;
    }

    public QueryStructure<T> setPagination(int pageNumber, int pageSize) {
        this.offset = (pageNumber - 1) * pageSize;
        this.limit = pageSize;
        return this;
    }

    public QueryStructure<T> addOrder(OrderBy orderBy) {
        orderByList.add(orderBy);
        return this;
    }

    public QueryStructure<T> groupBy(String... groupByFields) {
        groupBy = GroupBy.of(groupByFields);
        return this;
    }

    public QueryStructure<T> setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public QueryStructure<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public <T extends Storable> Class<T> entityType() {
        return (Class<T>) entityType;
    }

    public Optional<String[]> queryFields() {
        return Optional.ofNullable(fields == null || fields.length == 0
                ? null : fields);
    }

    public Optional<CombinedExpression> filterCondition() {
        return Optional.ofNullable(condition);
    }

    public Optional<OrderBy[]> orders() {
        if (orderByList.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(orderByList.toArray(new OrderBy[0]));
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