package com.turding.sponge.database;

import com.turding.sponge.core.Entity;
import com.turding.sponge.util.ObjectUtil;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yunfeng.pan on 17-6-17.
 */
public class ResultSetHandler<T> implements ResultSetCallback<T> {

    private Class<T> clazz;
    private List<Entity.Field> selectFields;

    public ResultSetHandler(Class<T> clazz, List<Entity.Field> selectFields) {
        this.clazz = clazz;
        this.selectFields = selectFields;
    }

    @Override
    public List<T> handle(ResultSet resultSet) throws Exception {
        List<T> resultList = new ArrayList<>();
        Map<String, Method> fieldNameSetterMapping = ObjectUtil.fieldNameAndSetterMapping(clazz);
        while (resultSet.next()) {
            resultList.add(wrap(resultSet, fieldNameSetterMapping));
        }
        return resultList;
    }

    public T wrap(ResultSet resultSet, Map<String, Method> fieldNameSetterMapping) throws Exception {
        T entity = clazz.newInstance();
        Method setMethod;
        for (Entity.Field f : selectFields) {
            setMethod = fieldNameSetterMapping.get(f.getFieldName());
            if (setMethod == null) {
                continue;
            }
            setMethod.invoke(entity, getValue(resultSet, setMethod.getParameterTypes()[0], f.getStoreName()));
        }
        return entity;
    }

    private Object getValue(ResultSet resultSet, Class<?> paramType, String column) throws Exception {
        if (paramType == String.class) {
            return resultSet.getString(column);
        }
        if (paramType == Integer.class) {
            return resultSet.getInt(column);
        }
        if (paramType == Long.class) {
            return resultSet.getLong(column);
        }
        if (paramType == Short.class) {
            return resultSet.getShort(column);
        }
        if (paramType == Float.class) {
            return resultSet.getFloat(column);
        }
        if (paramType == Double.class) {
            return resultSet.getDouble(column);
        }
        if (paramType == Byte.class) {
            return resultSet.getByte(column);
        }
        if (paramType == Boolean.class) {
            return resultSet.getBoolean(column);
        }
        if (paramType == BigDecimal.class) {
            return resultSet.getBigDecimal(column);
        }
        if (paramType == Timestamp.class
                || paramType == Date.class) {
            return resultSet.getTimestamp(column);
        }
        if (paramType == LocalDate.class) {
            Date date = resultSet.getDate(column);
            if (date == null) {
                return null;
            }
            return resultSet.getDate(column).toLocalDate();
        }
        if (paramType == LocalDateTime.class) {
            Date date = resultSet.getTimestamp(column);
            if (date == null) {
                return null;
            }
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }

        return resultSet.getObject(column);
    }

}
