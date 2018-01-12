package com.turding.sponge.database;

import com.turding.sponge.core.*;
import com.turding.sponge.util.ObjectUtil;
import com.turding.sponge.util.StringUtil;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by yunfeng.pan on 17-8-18.
 */
public class SqlSession {

    /**
     * 数据库方言
     */
    private DatabaseDialect dialect;

    private DataSource dataSource;

    public SqlSession(DataSource dataSource, String dialect) {
        this.dataSource = dataSource;
        this.dialect = dialect == null ? DatabaseDialect.MYSQL : DatabaseDialect.valueOf(dialect);
    }

    public SqlSession(DataSource dataSource, DatabaseDialect dialect) {
        this.dataSource = dataSource;
        this.dialect = dialect == null ? DatabaseDialect.MYSQL : dialect;
    }

    public static SqlSession of(DataSource dataSource) {
        return new SqlSession(dataSource, (DatabaseDialect) null);
    }

    /**
     * 条件查询
     * @param t 封装了查询条件的存储对象，忽略null值
     * @param <T>
     * @return
     */
    public <T extends Storable> List<T> select(T t) {
        Entity<T> entity = EntityParser.of(t).parse().result();
        List<Entity.Field> storableFields = entity.getStorableFields();
        QueryStructure queryStructure = QueryStructure.of(entity);
        CombinedExpression condition = null;
        boolean isFirst = true;
        for (Entity.Field storableField : storableFields) {
            if (storableField.getValue() == null) {
                continue;
            }
            if (isFirst) {
                isFirst = false;
                condition = CombinedExpression.of(Exps.eq(storableField.getFieldName(), storableField.getValue()));
            }
            condition.and(Exps.eq(storableField.getFieldName(), storableField.getValue()));
        }
        queryStructure.filterExp(condition);
        return select(queryStructure);
    }

    public <T extends Storable> List<T> select(Class<T> entityClass) {
        return select(QueryStructure.of(entityClass));
    }

    public <T extends Storable> List<T> select(Entity<T> entity) {
        return select(QueryStructure.of(entity));
    }

    public <T> List<T> select(QueryStructure queryStructure) {
        SqlQueryStructureParser.Result result = SqlQueryStructureParser.of(queryStructure).parse().result();
        return select(result.prepareSql(), result.prepareValues(), queryStructure.entity());
    }

    public <T> List<T> select(QueryStructure queryStructure, Class<T> resultBeanType) {
        SqlQueryStructureParser.Result result = SqlQueryStructureParser.of(queryStructure).parse().result();
        return select(result.prepareSql(), result.prepareValues(), resultBeanType);
    }

    public <T> List<T> select(String selectSql, Class<T> resultBeanType) {
        return select(selectSql, null, resultBeanType);
    }

    public <T> List<T> select(String prepareSelectSql,
                              List<Object> prepareValues,
                              Class<T> resultBeanType) {
        return select(prepareSelectSql, prepareValues,
                resultBeanType, null);
    }

    public <T> List<T> select(String prepareSelectSql,
                              List<Object> prepareValues,
                              Class<T> resultBeanType,
                              Map<String, String> selectFieldsMapping) {
        return Database.select(dataSource, prepareSelectSql, prepareValues,
                new ResultSetHandler(resultBeanType, selectFieldsMapping));
    }

    public <T extends Storable> List<T> select(String selectSql, Entity<T> entity) {
        return select(selectSql, null, entity);
    }

    public <T extends Storable> List<T> select(String prepareSelectSql,
                                               List<Object> prepareValues,
                                               Entity<T> entity) {
        return Database.select(dataSource, prepareSelectSql, prepareValues,
                new ResultSetHandler(entity.getType(), entity.getSearchableFields()));
    }

    /**
     * 条件查询
     * @param t 封装了查询条件的存储对象，忽略null值
     * @param <T>
     * @return
     */
    public <T extends Storable> T selectOne(T t) {
        return selectOne(QueryStructure.of(t.getClass()));
    }

    public <T extends Storable> T selectOne(Class<T> entityClass) {
        return selectOne(QueryStructure.of(entityClass));
    }

    public <T extends Storable> T selectOne(Entity<T> entity) {
        return selectOne(QueryStructure.of(entity));
    }

    public <T> T selectOne(QueryStructure queryStructure) {
        Class<T> resultBeanType = queryStructure.entity().getType();
        return selectOne(queryStructure, resultBeanType);
    }

    public <T> T selectOne(QueryStructure queryStructure, Class<T> resultBeanType) {
        SqlQueryStructureParser.Result result = SqlQueryStructureParser.of(queryStructure).parse().result();
        return selectOne(result.prepareSql(), result.prepareValues(), resultBeanType);
    }

    public <T> T selectOne(String selectSql, Class<T> resultBeanType) {
        return selectOne(selectSql, null, resultBeanType);
    }

    public <T> T selectOne(String prepareSelectSql,
                              List<Object> prepareValues,
                              Class<T> resultBeanType) {
        return selectOne(prepareSelectSql, prepareValues,
                resultBeanType, (Map<String, String> ) null);
    }

    public <T> T selectOne(String prepareSelectSql,
                           List<Object> prepareValues,
                           Class<T> resultBeanType,
                           Map<String, String> selectFieldsMapping) {
        List<T> result = Database.select(dataSource, prepareSelectSql, prepareValues,
                new ResultSetHandler(resultBeanType, selectFieldsMapping));
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public <T extends Storable> T selectOne(String selectSql, Entity<T> entity) {
        return selectOne(selectSql, null, entity);
    }

    public <T extends Storable> T selectOne(String prepareSelectSql,
                                               List<Object> prepareValues,
                                               Entity<T> entity) {
        List<T> result = Database.select(dataSource, prepareSelectSql, prepareValues,
                new ResultSetHandler(entity.getType(), entity.getSearchableFields()));
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public <T> List<T> selectSingleColumnValue(String selectSql, String columnName, Class<T> columnDataType) {
        return Database.select(dataSource, selectSql, null,
                new SingleValueResultSetHandler(columnName, columnDataType));
    }

    public <T> List<T> selectSingleColumnValue(String prepareSelectSql,
                                         List<Object> prepareValues,
                                         String columnName,
                                         Class<T> columnDataType) {
        return Database.select(dataSource, prepareSelectSql, prepareValues,
                new SingleValueResultSetHandler(columnName, columnDataType));
    }

    public <T extends Storable> void insert(T entity) {
        insertOrUpdate(entity, null);
    }

    /**
     * 依赖 ON DUPLICATE KEY UPDATE 语法实现。如数据库支持请小心谨慎。
     * @param entityInstance
     * @param onDuplicateSqlPart
     * @param <T>
     */
    public <T extends Storable> void insertOrUpdate(T entityInstance, String onDuplicateSqlPart) {
        if (entityInstance == null) {
            return;
        }

        Entity<T> entity = EntityParser.of(entityInstance).parse().result();
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

        if (!StringUtil.isBlank(onDuplicateSqlPart)) {
            sql.append(" ").append(onDuplicateSqlPart);
        }

        List<Long> generatedKeyList = Database.insert(dataSource, sql.toString(), storeFieldValueList);

        Entity.Field autoIncField;
        if ((autoIncField = entity.getAutoIncrementField()) == null
                || generatedKeyList.isEmpty()) {
            return;
        }
        setGeneratedKeyValue(autoIncField, entityInstance, generatedKeyList.get(0));
    }

    public <T extends Storable> void insertAll(List<T> entityInstanceList) {
        insertOrUpdateAll(entityInstanceList, null);
    }

    /**
     * 依赖 ON DUPLICATE KEY UPDATE 语法实现。如数据库不支持请小心谨慎。
     * @param entityInstanceList
     * @param onDuplicateSqlPart
     * @param <T>
     */
    public <T extends Storable> void insertOrUpdateAll(List<T> entityInstanceList, String onDuplicateSqlPart) {
        if (entityInstanceList == null || entityInstanceList.isEmpty()) {
            return;
        }
        List<Object> filedValues = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        List<Entity<T>> entityList = entityInstanceList.stream()
                .map(entityInstance -> EntityParser.of(entityInstance).parse().result())
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

        if (!StringUtil.isBlank(onDuplicateSqlPart)) {
            sql.append(" ").append(onDuplicateSqlPart);
        }

        List<Long> generatedKeyList = Database.insert(dataSource, sql.toString(), filedValues);

        Entity<T> entity = entityList.get(0);
        Entity.Field autoIncField = entity.getAutoIncrementField();
        if (autoIncField == null) {
            return;
        }

        for (int i = 0, len = generatedKeyList.size(); i < len; i++) {
            setGeneratedKeyValue(autoIncField, entityInstanceList.get(i), generatedKeyList.get(i));
        }
    }

    public <T extends Storable> int updateByPK(T entityInstance) {
        if (entityInstance == null) {
            return 0;
        }

        Entity<T> entity = EntityParser.of(entityInstance).parse().result();
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

    public <T extends Storable> int update(T entityInstance, CombinedExpression condition) {
        if (entityInstance == null) {
            return 0;
        }

        Entity<T> entity = EntityParser.of(entityInstance).parse().result();
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

    public <T extends Storable> int deleteByPK(T entityInstance) {
        if (entityInstance == null) {
            return 0;
        }
        Entity<T> entity = EntityParser.of(entityInstance).parse().result();
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

    public List<Long> insert(String insertSql) {
        return insert(insertSql, null);
    }

    public List<Long> insert(String prepareInsertSql, List<Object> prepareValues) {
        return Database.executeInsert(dataSource, prepareInsertSql, prepareValues);
    }

    public int update(String updateSql) {
        return update(updateSql, null);
    }

    public int update(String prepareUpdateSql, List<Object> prepareValues) {
        return Database.executeUpdate(dataSource, prepareUpdateSql, prepareValues);
    }

    public int delete(String deleteSql) {
        return delete(deleteSql, null);
    }

    public int delete(String prepareDeleteSql, List<Object> prepareValues) {
        return Database.executeUpdate(dataSource, prepareDeleteSql, prepareValues);
    }

    public boolean execute(String sql) {
        return Database.execute(dataSource, sql);
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
