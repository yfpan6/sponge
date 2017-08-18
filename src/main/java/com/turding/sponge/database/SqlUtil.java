package com.turding.sponge.database;

import java.sql.Time;
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
