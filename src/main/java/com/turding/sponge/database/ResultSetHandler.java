package com.turding.sponge.database;

import com.turding.sponge.core.Entity;
import com.turding.sponge.util.ObjectUtil;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * Created by yunfeng.pan on 17-6-17.
 */
public class ResultSetHandler<T> implements ResultSetCallback<T> {

    private enum BeanType {MAP, LIST, POJO}

    /**
     * 返回数据类型
     */
    private Class<T> clazz;

    private BeanType beanType;

    /**
     * 查询SQL语句选择列列名和返回对象属性名的映射关系。
     * 1、当selectFields未指定或其值为空且class 类型为普通java bean时，
     *    系统将解析 clazz 类中的所有public setter方法，并用setter方法构建默认selectFields。
     *    <p>如 SimpleStore 类中有方法
     *      public void setUUID(String uuid) 系统将解析出 uUID：uUID
     *      public void setMyName(String myName) 系统将解析出 myName：myName
     *    </p>
     * 2、当selectFields未指定或其值为空且class 类型为Map时，
     *    系统直接以SQL选择列列名作为返回结果的key值。
     * 3、当selectFields未指定或其值为空且class 类型为List时，
     *    系统直接以SQL选择列列名作为返回结果的key值。
     * key -> StoreName
     * value -> FieldName
     */
    private Map<String, String> selectFields;

    public ResultSetHandler(Class<T> clazz, Map<String, String> selectFields) {
        this.clazz = clazz;
        if (Map.class.isAssignableFrom(clazz)) {
            beanType = BeanType.MAP;
        } else if (List.class.isAssignableFrom(clazz)) {
            beanType = BeanType.LIST;
        } else {
            beanType = BeanType.POJO;
        }
        this.selectFields = selectFields == null ? new HashMap<>() : selectFields;
    }

    public ResultSetHandler(Class<T> clazz, List<Entity.Field> entityFields) {
        this(clazz, (Map<String, String>) null);
        if (entityFields != null) {
            entityFields.forEach(field -> {
                if (field.isSearchable()) {
                    selectFields.put(field.getStoreName(), field.getFieldName());
                }
            });
        }
    }

    @Override
    public List<T> handle(ResultSet resultSet) throws Exception {
        if (beanType == BeanType.MAP) {
            return toMapList(resultSet);
        } else if (beanType == BeanType.LIST) {
            return toListList(resultSet);
        }
        return toPojoList(resultSet);
    }

    private List<T> toMapList(ResultSet resultSet) throws Exception {
        List<T> resultList = new ArrayList<>();

        fillSelectFieldsWithColumnNames(resultSet);

        Set<Map.Entry<String, String>> entrySet = selectFields.entrySet();
        T entity;
        while (resultSet.next()) {
            entity = clazz.newInstance();
            for (Map.Entry<String, String> entry : entrySet) {
                ((Map) entity).put(entry.getValue(), resultSet.getObject(entry.getKey()));
            }
            resultList.add(entity);
        }

        return resultList;
    }

    private List<T> toListList(ResultSet resultSet) throws Exception {
        List<T> resultList = new ArrayList<>();

        fillSelectFieldsWithColumnNames(resultSet);

        Set<Map.Entry<String, String>> entrySet = selectFields.entrySet();
        T entity;
        while (resultSet.next()) {
            entity = clazz.newInstance();
            for (Map.Entry<String, String> entry : entrySet) {
                ((List) entity).add(resultSet.getObject(entry.getKey()));
            }
            resultList.add(entity);
        }

        return resultList;
    }

    private void fillSelectFieldsWithColumnNames(ResultSet resultSet) throws Exception {
        if (selectFields.isEmpty()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columCount = metaData.getColumnCount();
            for (int i = 1; i <= columCount; i++) {
                selectFields.put(metaData.getColumnLabel(i), metaData.getColumnLabel(i));
            }
        }
    }

    public List<T> toPojoList(ResultSet resultSet) throws Exception {
        List<T> resultList = new ArrayList<>();

        Map<String, Method> fieldNameSetterMapping = ObjectUtil.fieldNameAndSetterMapping(clazz);
        if (selectFields.isEmpty()) {
            fieldNameSetterMapping.forEach((field, method) -> {
                selectFields.put(field, field);
            });
        }

        Set<Map.Entry<String, String>> entrySet = selectFields.entrySet();
        Method setMethod;
        T entity;
        while (resultSet.next()) {
            entity = clazz.newInstance();
            for (Map.Entry<String, String> entry : entrySet) {
                setMethod = fieldNameSetterMapping.get(entry.getValue());
                setMethod.invoke(entity, SqlUtil.getValue(resultSet,
                        setMethod.getParameterTypes()[0],
                        entry.getKey()));
            }
            resultList.add(entity);
        }

        return resultList;
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();

        System.out.println(Map.class.isAssignableFrom(LinkedHashMap.class));
    }

}
