package com.turding.sponge.util;

import java.util.Map;

/**
 * Created by yunfeng.pan on 2017-09-01.
 */
public class Sqls {

    private static final char defPlaceholderPrefix = '$';

    public static String replacePlaceholder(String sql, Map<String, Object> params) {
        return replacePlaceholder(sql, params, defPlaceholderPrefix);
    }

    public static String replacePlaceholder(String sql,
                                      Map<String, Object> params,
                                      char placeholderPrefix) {
        if (sql == null || sql.indexOf(placeholderPrefix + "{") == -1) {
            return sql;
        }

        StringBuilder newSql = new StringBuilder();
        StringBuilder paramName = new StringBuilder();
        int start = 0;
        char c;
        for (int i = 0; i < sql.length(); i++) {
            c = sql.charAt(i);
            if (c == placeholderPrefix) {
                start++;
                continue;
            }

            if (start == 0) {
                newSql.append(c);
                continue;
            }

            if (start == 1) {
                if (c == '{') {
                    start++;
                    continue;
                }
                start = 0;
                newSql.append(placeholderPrefix).append(c);
                continue;
            }

            if (start == 2 && c == '}') {
                Object value = params.get(paramName.toString());
                if (value instanceof String) {
                    newSql.append('\'').append(value).append('\'');
                } else {
                    newSql.append(value);
                }
                start = 0;
                paramName.setLength(0);
                continue;
            }

            if (' ' == c) {
                continue;
            }

            paramName.append(c);
        }

        return newSql.toString();
    }

}
