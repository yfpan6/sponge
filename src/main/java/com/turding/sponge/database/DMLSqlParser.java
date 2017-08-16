package com.turding.sponge.database;

import com.turding.sponge.core.CombinedExpression;
import com.turding.sponge.core.Entity;
import com.turding.sponge.core.EntityParser;
import com.turding.sponge.core.Storable;
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
    private List<Entity.Field> storableFieldList;
    private ParsedDMLSqlParser emptyParsedDMLSqlParser;

    public DMLSqlParser(Entity<T> entity, T entityBean) {
        this.entity = entity;
        this.entityBean = entityBean;
        this.storableFieldList = entity.getStorableFieldList();
        this.emptyParsedDMLSqlParser = new ParsedDMLSqlParser(null);
    }

    public static <T extends Storable> DMLSqlParser of(T entityBean) {
        if (entityBean == null) {
            throw new NullPointerException("input parameter[entityBean] is null.");
        }
        Entity<T> entity = EntityParser.of(entityBean).parse().result();
        return new DMLSqlParser(entity, entityBean);
    }

    public ParsedDMLSqlParser parseForInsert() {
        if (storableFieldList.size() == 0) {
            return emptyParsedDMLSqlParser;
        }

        List<Object> prepareValueList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ");
        sql.append(entity.getStoreTarget());
        sql.append("(");
        storableFieldList.forEach(field -> {
            sql.append(field.getStoreName()).append(",");
            prepareValueList.add(field.getValue());
        });
        sql.setLength(sql.length() - 1);
        sql.append(") values(");
        int colLen = storableFieldList.size();
        for (int i = 0; i < colLen; i++) {
            sql.append("?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(")");

        Result result = new Result();
        result.prepareSql = sql.toString();
        result.prepareValueList = prepareValueList;
        return new ParsedDMLSqlParser(result);
    }

    public ParsedDMLSqlParser parserForDeleteById() {
        if (storableFieldList.size() == 0) {
            return emptyParsedDMLSqlParser;
        }
        List<Entity.Field> pkList = entity.getPkList();
        if(pkList.isEmpty()) {
            throw new NullPointerException("entity[" + entity.getType() + "] hasn't pk fields");
        }

        List<Object> prepareValueList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ")
                .append(entity.getStoreTarget())
                .append(" where ");
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

    public ParsedDMLSqlParser parseForDelete(CombinedExpression condition) {
        if (storableFieldList.size() == 0) {
            return emptyParsedDMLSqlParser;
        }
        List<Object> prepareValueList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ")
                .append(entity.getStoreTarget())
                .append(" where ");
        SqlConditionParser.Result parser = SqlConditionParser.of(condition, entity)
                .parse().result();
        sql.append(parser.getPrepareSql());

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
