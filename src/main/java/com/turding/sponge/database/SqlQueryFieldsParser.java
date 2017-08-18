package com.turding.sponge.database;

import com.turding.sponge.core.Entity;
import com.turding.sponge.core.EntityParser;
import com.turding.sponge.core.QueryField;
import com.turding.sponge.core.Storable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析select字段
 *
 * Created by yunfeng.pan on 17-6-19.
 */
public final class SqlQueryFieldsParser {

    private Entity entity;
    private QueryField[] fields;
    private Result result;
    private ParsedSqlQueryFieldsParser parsedSqlQueryFieldsParser;

    private SqlQueryFieldsParser(Entity entity, QueryField[] fields) {
        this.entity = entity;
        this.fields = fields;
        this.parsedSqlQueryFieldsParser = new ParsedSqlQueryFieldsParser();
    }

    public static SqlQueryFieldsParser of(Entity entity, QueryField[] fields) {
        return new SqlQueryFieldsParser(entity, fields);
    }

    public static <T extends Storable> SqlQueryFieldsParser of(T storeEntity, QueryField[] fields) {
        return new SqlQueryFieldsParser(EntityParser.of(storeEntity).parse().result(), fields);
    }

    public static <T extends Storable> SqlQueryFieldsParser of(Class<T> storeEntityType, QueryField[] fields) {
        return new SqlQueryFieldsParser(EntityParser.of(storeEntityType).parse().result(), fields);
    }

    public ParsedSqlQueryFieldsParser parse() {
        List<String> selectFields = new ArrayList<>();
        List<Entity.Field> entityFields = new ArrayList<>();
        Entity.Field entityField;
        String selectField, fieldName, fieldExp, alias;
        for (int i = 0; i < fields.length; i++) {
            alias = fields[i].getAlias();
            fieldExp = SqlExpressionParser
                    .of(entity, fields[i].getFieldExp())
                    .parse()
                    .result()
                    .getRawSql();
            if (alias != null) {
                fieldName = alias;
                selectField = fieldExp + " AS " + alias;
            } else {
                fieldName = fieldExp;
                selectField = fieldExp;
            }

            entityField = entity.getFieldByFieldName(fieldName);
            if ( entity.getFieldByFieldName(fieldName) == null) {
                throw new IllegalArgumentException("query field [" + fieldName + "] not  existing.");
            }
            selectFields.add(selectField);
            entityFields.add(entityField);
        }

        result = new Result();
        result.selectFields = selectFields;
        result.entityFields = entityFields;
        return parsedSqlQueryFieldsParser;
    }

    public static final class Result {
        /**
         * 获取选择字段字段列表.
         *
         * @return
         */
        @Getter
        private List<String> selectFields;
        @Getter
        private List<Entity.Field> entityFields;

    }

    public final class ParsedSqlQueryFieldsParser {
        public Result result() {
            return result;
        }
    }

}
