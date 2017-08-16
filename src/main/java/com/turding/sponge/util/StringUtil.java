package com.turding.sponge.util;

import java.util.Optional;

/**
 * Created by yunfeng.pan on 17-6-14.
 */
public class StringUtil {

    public static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    public static final String GETTER_NAME_PREFIX = "get";

    public static String toCamel(String snake) {
        return null;
    }

    public static Optional<String> getMethodNameToFiledName(String getMethodName) {
        if (isBlank(getMethodName)
                || !getMethodName.startsWith(GETTER_NAME_PREFIX)) {
            return Optional.empty();
        }
        char[] chars = getMethodName.toCharArray();
        int pos = GETTER_NAME_PREFIX.length();
        chars[pos] = charToLower(chars[pos]);
        char[] dest = new char[chars.length - pos];
        System.arraycopy(chars, pos, dest, 0, dest.length);
        return Optional.of(new String(dest));
    }

    /**
     * camel to snake
     *
     * @param camel
     * @return
     */
    public static String toSnake(String camel) {
        if (isBlank(camel)) {
            return camel;
        }
        StringBuilder snake = new StringBuilder();
        char c = camel.charAt(0);
        snake.append(charToLower(c));

        for (int i = 1, camelLen = camel.length(); i < camelLen; i++) {
            c = camel.charAt(i);
            if (isCharUpper(c)) {
                snake.append("_").append((char) (c + 32));
            } else {
                snake.append(c);
            }
        }

        return snake.toString();
    }

    /**
     * 判断字符是否是一个大写字母
     *
     * @param c
     * @return
     */
    public static boolean isCharUpper(char c) {
        return c >= 'A' && c <= 'Z';
    }

    /**
     * 判断字符是否是一个小写字母
     *
     * @param c
     * @return
     */
    public static boolean isCharLower(char c) {
        return c >= 'a' && c <= 'z';
    }


    /**
     * 如果是大写字母，则转换未小写
     *
     * @param c
     * @return
     */
    public static char charToLower(char c) {
        if (isCharUpper(c)) {
            return (char) (c + 32);
        }

        return c;
    }

    public static char charToUpper(char c) {
        if (isCharLower(c)) {
            return (char) (c - 32);
        }

        return c;
    }

}
