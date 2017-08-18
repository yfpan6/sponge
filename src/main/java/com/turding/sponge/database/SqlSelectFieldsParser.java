package com.turding.sponge.database;

import com.turding.sponge.core.Entity;
import com.turding.sponge.core.EntityParser;
import com.turding.sponge.core.QueryField;
import com.turding.sponge.core.Storable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 解析select字段
 *
 * Created by yunfeng.pan on 17-6-19.
 */
public final class SqlSelectFieldsParser {

    private Entity entity;
    private QueryField[] fields;
    private Result result;
    private ParsedSqlSelectFieldsParser parsedSqlSelectFieldsParser;

    private SqlSelectFieldsParser(Entity entity, QueryField[] fields) {
        this.entity = entity;
        this.fields = fields;
        this.parsedSqlSelectFieldsParser = new ParsedSqlSelectFieldsParser();
    }

    public static SqlSelectFieldsParser of(Entity entity, QueryField[] fields) {
        return new SqlSelectFieldsParser(entity, fields);
    }

    public static <T extends Storable> SqlSelectFieldsParser of(T storeEntity, QueryField[] fields) {
        return new SqlSelectFieldsParser(EntityParser.of(storeEntity).parse().result(), fields);
    }

    public static <T extends Storable> SqlSelectFieldsParser of(Class<T> storeEntityType, QueryField[] fields) {
        return new SqlSelectFieldsParser(EntityParser.of(storeEntityType).parse().result(), fields);
    }

    public ParsedSqlSelectFieldsParser parse() {
        List<Entity.Field> selectFieldList = new ArrayList<>();
        Entity.Field entityField;
        String fieldName = null, alias;
        for (int i = 0; i < fields.length; i++) {
            alias = fields[i].getFieldName();
            if (fields[i].getStoreFieldExp() != null) {
                fieldName = SqlExpressionParser
                        .of(fields[i].getStoreFieldExp(), entity)
                        .parse()
                        .result()
                        .getRawSql();
            }
            alias = alias == null ? fieldName : null;
            entityField = entity.getFieldByFieldName(alias);
            if (entityField == null) {
                throw new IllegalArgumentException("query field [" + alias + "] not  existing.");
            }
            selectFieldList.add(entityField);
        }

        result = new Result();
        result.selectFieldList = selectFieldList;
        return parsedSqlSelectFieldsParser;
    }

    public static final class Result {
        private List<Entity.Field> selectFieldList;

        /**
         * 获取选择字段字段列表.
         *
         * @return
         */
        public List<Entity.Field> getEntityFieldList() {
            return selectFieldList;
        }

        /**
         * 获取选择字段字段名列表.
         *
         * @return
         */
        public List<String> getFieldNameList() {
            return selectFieldList.stream()
                    .map(Entity.Field::getFieldName)
                    .collect(Collectors.toList());
        }

        /**
         * 获取选择字段存储名列表.
         *
         * @return
         */
        public List<String> getStoreNameList() {
            return selectFieldList.stream()
                    .map(Entity.Field::getStoreName)
                    .collect(Collectors.toList());
        }
    }

    public final class ParsedSqlSelectFieldsParser {
        public Result result() {
            return result;
        }
    }

}
