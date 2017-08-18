package com.turding.sponge.database;

import com.turding.sponge.core.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 从实体对象总解析出 DML sql及相关参数.
 *
 * Created by yunfeng.pan on 17-6-20.
 */
public class DMLSqlParser<T extends Storable> {

    private Entity<T> entity;
    private T entityBean;
    private List<Entity.Field> storableFields;
    private ParsedDMLSqlParser emptyParsedDMLSqlParser;

    public DMLSqlParser(Entity<T> entity, T entityBean) {
        this.entity = entity;
        this.entityBean = entityBean;
        this.storableFields = entity.getStorableFields();
        this.emptyParsedDMLSqlParser = new ParsedDMLSqlParser(null);
    }

    public static <T extends Storable> DMLSqlParser of(T entityBean) {
        if (entityBean == null) {
            throw new NullPointerException("input parameter[entityBean] is null.");
        }
        Entity<T> entity = EntityParser.of(entityBean).parse().result();
        return new DMLSqlParser(entity, entityBean);
    }

    public static <T extends Storable> DMLSqlParser of(Entity<T> entity) {
        if (entity == null) {
            throw new NullPointerException("input parameter[entity] is null.");
        }
        return new DMLSqlParser(entity, null);
    }

    public ParsedDMLSqlParser parseForInsert() {
        if (storableFields.size() == 0) {
            return emptyParsedDMLSqlParser;
        }

        List<Object> prepareValues = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(entity.getStoreTarget());
        sql.append("(");
        storableFields.forEach(field -> {
            sql.append(field.getStoreName()).append(",");
            prepareValues.add(field.getValue());
        });
        sql.setLength(sql.length() - 1);
        sql.append(") VALUES(");
        int colLen = storableFields.size();
        for (int i = 0; i < colLen; i++) {
            sql.append("?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(")");

        Result result = new Result();
        result.prepareSql = sql.toString();
        result.prepareValueList = prepareValues;
        return new ParsedDMLSqlParser(result);
    }

    public ParsedDMLSqlParser parserForDeleteById() {
        if (storableFields.size() == 0) {
            return emptyParsedDMLSqlParser;
        }
        List<Entity.Field> pkList = entity.getPks();
        if(pkList.isEmpty()) {
            throw new NullPointerException("entity[" + entity.getType() + "] hasn't pk fields");
        }

        List<Object> prepareValueList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ")
                .append(entity.getStoreTarget())
                .append(" WHERE ");
        pkList.forEach(pkField -> {
            sql.append(pkField.getStoreName()).append(" = ?,");
            prepareValueList.add(pkField.getValue());
        });
        sql.setLength(sql.length() - 1);

        Result result = new Result();
        result.prepareSql = sql.toString();
        result.prepareValueList = prepareValueList;
        return new ParsedDMLSqlParser(result);
    }

    public ParsedDMLSqlParser parseForDelete(Expression condition) {
        if (storableFields.size() == 0) {
            return emptyParsedDMLSqlParser;
        }
        List<Object> prepareValueList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ")
                .append(entity.getStoreTarget());
        if (condition != null) {
            sql.append(" WHERE ");
            SqlExpressionParser.Result parser = SqlExpressionParser.of(entity, condition)
                    .parse().result();
            sql.append(parser.getPrepareSql());
            prepareValueList.addAll(parser.getPrepareValues());
        }

        Result result = new Result();
        result.prepareSql = sql.toString();
        result.prepareValueList = prepareValueList;
        return new ParsedDMLSqlParser(result);
    }

    public static final class Result {
        @Getter
        private String prepareSql;
        @Getter
        private List<Object> prepareValueList;
    }

    public final class ParsedDMLSqlParser {

        private Result result;

        private ParsedDMLSqlParser(Result result) {
            this.result = result;
        }

        public Result result() {
            return result;
        }
    }
}
