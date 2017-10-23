package com.turding.sponge.database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yunfeng.pan on 17-6-17.
 */
public class MapResultSetHandler implements ResultSetCallback<Map<String, Object>> {

    private String[] columNames;

    public MapResultSetHandler() {
    }

    public MapResultSetHandler(String... columNames) {
        this.columNames = columNames;
    }

    @Override
    public List<Map<String, Object>> handle(ResultSet resultSet) throws Exception {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (columNames == null || columNames.length == 0) {
            ResultSetMetaData rsMetaData = resultSet.getMetaData();
            int columnCount = rsMetaData.getColumnCount();
            Map<String, Object> item;
            while (resultSet.next()) {
                item = new LinkedHashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    item.put(rsMetaData.getColumnLabel(i), resultSet.getObject(i));
                }
                resultList.add(item);
            }
        } else {
            Map<String, Object> item;
            while (resultSet.next()) {
                item = new LinkedHashMap<>();
                for (String columName : columNames) {
                    item.put(columName, resultSet.getObject(columName));
                }
                resultList.add(item);
            }
        }
        return resultList;
    }

//    private Object getValue(ResultSet resultSet, Class<?> paramType, String column) throws Exception {
//        if (paramType == String.class) {
//            return resultSet.getString(column);
//        }
//        if (paramType == Integer.class || paramType == Integer.TYPE) {
//            return resultSet.getInt(column);
//        }
//        if (paramType == Long.class || paramType == Long.TYPE) {
//            return resultSet.getLong(column);
//        }
//        if (paramType == Short.class || paramType == Short.TYPE) {
//            return resultSet.getShort(column);
//        }
//        if (paramType == Float.class || paramType == Float.TYPE) {
//            return resultSet.getFloat(column);
//        }
//        if (paramType == Double.class || paramType == Double.TYPE) {
//            return resultSet.getDouble(column);
//        }
//        if (paramType == Byte.class || paramType == Byte.TYPE) {
//            return resultSet.getByte(column);
//        }
//        if (paramType == Boolean.class || paramType == Boolean.TYPE) {
//            return resultSet.getBoolean(column);
//        }
//        if (paramType == BigDecimal.class) {
//            return resultSet.getBigDecimal(column);
//        }
//        if (paramType == Timestamp.class
//                || paramType == Date.class) {
//            return resultSet.getTimestamp(column);
//        }
//        if (paramType == LocalDate.class) {
//            Date date = resultSet.getDate(column);
//            if (date == null) {
//                return null;
//            }
//            return resultSet.getDate(column).toLocalDate();
//        }
//        if (paramType == LocalDateTime.class) {
//            Date date = resultSet.getTimestamp(column);
//            if (date == null) {
//                return null;
//            }
//            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
//        }
//
//        return resultSet.getObject(column);
//    }

}
