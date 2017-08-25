package com.turding.sponge.database;

import com.turding.sponge.core.*;
import com.turding.sponge.util.ObjectUtil;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yunfeng.pan on 17-8-18.
 */
public class SqlSession {

    private DataSource dataSource;

    SqlSession(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static SqlSession of(DataSource dataSource) {
        return new SqlSession(dataSource);
    }

    public <T extends Storable> List<T> select(T t) {
        return select(QueryStructure.of((Class<T>) t.getClass()));
    }

    public <T extends Storable> List<T> select(Class<T> tClass) {
        return select(QueryStructure.of(tClass));
    }

    public <T extends Storable> List<T> select(QueryStructure<T> queryStructure) {
        SqlQueryStructureParser.Result result = SqlQueryStructureParser.of(queryStructure).parse().result();
        return Database.select(dataSource, result.prepareSql(), result.prepareValues(),
                new ResultSetHandler(queryStructure.entityType(), result.entityFields()));
    }

    public <T extends Storable> void insert(T logEntity) {
        if (logEntity == null) {
            return;
        }

        Entity<T> entity = EntityParser.of(logEntity).parse().result();
        List<Entity.Field> storeFieldList = entity.getStorableFields();
        if (storeFieldList.size() == 0) {
            return;
        }
        List<Object> storeFieldValueList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ");
        sql.append(entity.getStoreTarget());
        sql.append("(");
        storeFieldList.forEach(field -> {
            sql.append(field.getStoreName()).append(",");
            storeFieldValueList.add(field.getValue());
        });
        sql.setLength(sql.length() - 1);
        sql.append(") values(");
        int colLen = storeFieldList.size();
        for (int i = 0; i < colLen; i++) {
            sql.append("?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(")");

        List<Long> generatedKeyList = Database.insert(dataSource, sql.toString(), storeFieldValueList);

        Entity.Field autoIncField;
        if ((autoIncField = entity.getAutoIncrementField()) == null
                || generatedKeyList.isEmpty()) {
            return;
        }
        setGeneratedKeyValue(autoIncField, logEntity, generatedKeyList.get(0));
    }

    public <T extends Storable> void insertAll(List<T> logEntityList) {
        if (logEntityList == null || logEntityList.isEmpty()) {
            return;
        }
        List<Object> filedValues = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        List<Entity<T>> entityList = logEntityList.stream()
                .map(logEntity -> EntityParser.of(logEntity).parse().result())
                .collect(Collectors.toList());

        entityList.forEach(entity -> {
            List<Entity.Field> storeFieldList = entity.getStorableFields();
            if (storeFieldList.isEmpty()) {
                return;
            }

            List<Object> storeFieldValueList = new ArrayList<>();
            if (sql.length() == 0) {
                sql.append("insert into ");
                sql.append(entity.getStoreTarget());
                sql.append("(");
                storeFieldList.forEach(field -> {
                    sql.append(field.getStoreName()).append(",");
                });
                sql.setLength(sql.length() - 1);
                sql.append(") values");
            }
            sql.append("(");
            storeFieldList.forEach(field -> {
                sql.append("?,");
                storeFieldValueList.add(field.getValue());
            });
            sql.setLength(sql.length() - 1);
            sql.append("),");
            filedValues.addAll(storeFieldValueList);
        });

        if (sql.length() == 0) {
            return;
        }

        sql.setLength(sql.length() - 1);
        List<Long> generatedKeyList = Database.insert(dataSource, sql.toString(), filedValues);

        Entity<T> entity = entityList.get(0);
        Entity.Field autoIncField = entity.getAutoIncrementField();
        if (autoIncField == null) {
            return;
        }

        for (int i = 0, len = generatedKeyList.size(); i < len; i++) {
            setGeneratedKeyValue(autoIncField, logEntityList.get(i), generatedKeyList.get(i));
        }
    }

    public <T extends Storable> int updateByPK(T logEntity) {
        if (logEntity == null) {
            return 0;
        }

        Entity<T> entity = EntityParser.of(logEntity).parse().result();
        List<Entity.Field> updatableFieldList = entity.getUpdatableFields();
        List<Entity.Field> pkList = entity.getPks();
        if (updatableFieldList.size() == 0
                || pkList.size() == 0) {
            return 0;
        }

        List<Object> fieldValueList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(entity.getStoreTarget()).append(" set ");
        StringBuilder setSql = new StringBuilder();
        updatableFieldList.forEach(field -> {
            if (field.getValue() == null) {
                return;
            }
            setSql.append(field.getStoreName()).append(" = ?,");
            fieldValueList.add(field.getValue());
        });

        if (setSql.length() == 0) {
            return 0;
        }

        setSql.setLength(setSql.length() - 1);
        sql.append(setSql).append(" where ");
        pkList.forEach(pkField -> {
            sql.append(pkField.getStoreName()).append(" = ?,");
            fieldValueList.add(pkField.getValue());
        });
        sql.setLength(sql.length() - 1);
        return Database.update(dataSource, sql.toString(), fieldValueList);
    }

    public <T extends Storable> int update(T logEntity, CombinedExpression condition) {
        if (logEntity == null) {
            return 0;
        }

        Entity<T> entity = EntityParser.of(logEntity).parse().result();
        List<Entity.Field> updatableFieldList = entity.getUpdatableFields();
        if (updatableFieldList.size() == 0) {
            return 0;
        }

        List<Object> fieldValueList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(entity.getStoreTarget()).append(" set ");
        StringBuilder setSql = new StringBuilder();
        updatableFieldList.forEach(field -> {
            if (field.getValue() == null) {
                return;
            }
            setSql.append(field.getStoreName()).append(" = ? ,");
            fieldValueList.add(field.getValue());
        });

        if (setSql.length() == 0) {
            return 0;
        }

        setSql.setLength(setSql.length() - 1);
        sql.append(setSql).append(" where ");
        SqlExpressionParser.Result result = SqlExpressionParser.of(entity, condition).parse().result();
        sql.append(result.getPrepareSql());
        fieldValueList.addAll(result.getPrepareValues());
        return Database.update(dataSource, sql.toString(), fieldValueList);
    }

    public <T extends Storable> int deleteByPK(T logEntity) {
        if (logEntity == null) {
            return 0;
        }
        Entity<T> entity = EntityParser.of(logEntity).parse().result();
        List<Entity.Field> pkList = entity.getPks();
        if (pkList.isEmpty()) {
            return 0;
        }

        List<Object> fieldValueList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ")
                .append(entity.getStoreTarget())
                .append(" where ");

        pkList.forEach(pkField -> {
            sql.append(pkField.getStoreName()).append(" = ?,");
            fieldValueList.add(pkField.getValue());
        });
        sql.setLength(sql.length() - 1);
        return Database.delete(dataSource, sql.toString(), fieldValueList);
    }

    public <T extends Storable> int delete(Class<T> entityClass, CombinedExpression condition) {
        Entity<T> entity = EntityParser.of(entityClass).parse().result();
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ")
                .append(entity.getStoreTarget())
                .append(" where ");
        SqlExpressionParser.Result parser = SqlExpressionParser.of(entity, condition).parse().result();
        sql.append(parser.getPrepareSql());
        return Database.delete(dataSource, sql.toString(), parser.getPrepareValues());
    }

    /**
     * 为自增字段设置自增后的值
     *
     * @param autoIncField 自增字段
     * @param entityBean 实体 bean
     * @param generatedKeyValue 自增值
     */
    private void setGeneratedKeyValue(Entity.Field autoIncField,
                                      Object entityBean,
                                      Long generatedKeyValue) {
        try {
            Field field = ObjectUtil.getField(entityBean.getClass(), autoIncField.getFieldName()).get();
            Object value = ObjectUtil.convertLongToTargetType(field.getType(), generatedKeyValue);
            field.setAccessible(true);
            field.set(entityBean, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
