package com.turding.sponge.database;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Created by yunfeng.pan on 17-8-17.
 */
public class SqlUtil {

    private static final DateTimeFormatter defDateTimeFmtPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter defTimeFmtPattern = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static final Object getValue(ResultSet resultSet, Class<?> paramType, String column) throws Exception {
        if (paramType == String.class) {
            return resultSet.getString(column);
        }
        if (paramType == Integer.class || paramType == Integer.TYPE) {
            return resultSet.getInt(column);
        }
        if (paramType == Long.class || paramType == Long.TYPE) {
            return resultSet.getLong(column);
        }
        if (paramType == Short.class || paramType == Short.TYPE) {
            return resultSet.getShort(column);
        }
        if (paramType == Float.class || paramType == Float.TYPE) {
            return resultSet.getFloat(column);
        }
        if (paramType == Double.class || paramType == Double.TYPE) {
            return resultSet.getDouble(column);
        }
        if (paramType == Byte.class || paramType == Byte.TYPE) {
            return resultSet.getByte(column);
        }
        if (paramType == Boolean.class || paramType == Boolean.TYPE) {
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

    /**
     * 返回SQL 原语句.
     * 替换 prepareSql中的 '?' 值
     * @param prepareSql
     * @param prepareValues
     * @return
     */
    public static String toRawSql(String prepareSql, List<Object> prepareValues) {
        if (prepareSql == null) {
            return null;
        }

        if (prepareValues == null || prepareValues.isEmpty()) {
            return prepareSql;
        }

        if (prepareSql.indexOf('?') == -1) {
            return prepareSql;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0, pos = 0, len = prepareSql.length(); i < len; i++) {
            if (prepareSql.charAt(i) == '?') {
                Object value = prepareValues.get(pos++);
                if (value == null
                        || value instanceof Integer
                        || value instanceof Double
                        || value instanceof Long
                        || value instanceof Byte
                        || value instanceof Float
                        || value instanceof Short) {
                    result.append(value);
                    continue;
                }
                result.append('\'');
                if (value instanceof String) {
                    result.append(value);
                } else if (value instanceof Time) {
                    result.append(toTimeStr(((Time) value).toLocalTime()));
                } else if (value instanceof Date) {
                    // 时间处理
                    result.append(toDateStr(
                            LocalDateTime.ofInstant(((Date) value).toInstant(),
                                    ZoneId.systemDefault())));
                } else if (value instanceof LocalTime) {
                    result.append(toTimeStr((LocalTime) value));
                } else if (value instanceof LocalDateTime) {
                    result.append(toDateStr((LocalDateTime) value));
                } else if (value instanceof LocalDate) {
                    result.append(toDateStr((LocalDate) value));
                } else {
                    result.append(value);
                }
                result.append('\'');
            } else {
                result.append(prepareSql.charAt(i));
            }
        }
        return result.toString();
    }

    private static String toDateStr(LocalDateTime dateTime) {
        return dateTime.format(defDateTimeFmtPattern);
    }

    private static String toDateStr(LocalDate date) {
        return date.format(defDateTimeFmtPattern);
    }

    private static String toTimeStr(LocalTime time) {
        return  time.format(defTimeFmtPattern);
    }
}
