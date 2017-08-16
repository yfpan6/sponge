package com.turding.sponge.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yunfeng.pan on 17-6-14.
 */
public final class ObjectUtil {

    public static Optional<Field> getField(Class<?> clazz, String fieldName) {
        if (clazz == null) {
            return Optional.empty();
        }
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field == null) {
                return getField(clazz.getSuperclass(), fieldName);
            }
            return Optional.of(field);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("no this field[" +  fieldName + "] in " + clazz);
        }
    }

    public static <T> T convertLongToTargetType(Class<T> targetType, Long longValue) {
        if (targetType == Integer.class) {
            return (T) Integer.valueOf(longValue.intValue());
        } else if (targetType == Long.class) {
            return (T) longValue;
        } else if (targetType == Short.class) {
            return (T) Short.valueOf(longValue.shortValue());
        } else if (targetType == Byte.class) {
            return (T) Byte.valueOf(longValue.byteValue());
        } else if (targetType == Double.class) {
            return (T) Double.valueOf(longValue.doubleValue());
        } else if (targetType == Float.class) {
            return (T) Float.valueOf(longValue.floatValue());
        } else if (targetType == BigInteger.class) {
            return (T) BigInteger.valueOf(longValue);
        } else if (targetType == String.class) {
            return (T) String.valueOf(longValue);
        } else if (targetType == BigDecimal.class){
            return (T) BigDecimal.valueOf(longValue);
        } else {
            throw new ClassCastException("long can't case to " + targetType);
        }
    }

    public static Map<String, Method> fieldNameAndGetterMapping(Class<?> clazz) {
        return fieldNameAndGetterSetterMapping(clazz, true);
    }

    public static Map<String, Method> fieldNameAndSetterMapping(Class<?> clazz) {
        return fieldNameAndGetterSetterMapping(clazz, false);
    }

    private static Map<String, Method> fieldNameAndGetterSetterMapping(Class<?> clazz, boolean isGetter) {
        Map<String, Method> methodMap = new HashMap<String, Method>();
        String methodPrefix = isGetter ? "get" : "set";
        Method[] methods = clazz.getMethods();
        String methodName, tempMethodPrefix;
        for (int i = 0; i < methods.length; i++) {
            methodName = methods[i].getName();
            if (isGetter && methodName.startsWith("is")) {
                tempMethodPrefix = "is";
            } else {
                tempMethodPrefix = methodPrefix;
            }
            if (methodName.startsWith(tempMethodPrefix)) {
                int prefixLen = tempMethodPrefix.length();
                char[] chars = methodName.toCharArray();
                chars[3] = StringUtil.charToLower(chars[prefixLen]);
                char[] tempChars = new char[chars.length - prefixLen];
                System.arraycopy(chars, prefixLen, tempChars, 0, tempChars.length);
                methodMap.put(new String(tempChars), methods[i]);
            }
        }
        return methodMap;
    }

    /**
     * 提取对象属性和值.
     * <p>
     * 1、java bean 通过 getter 提取字段和值.
     * 2、map 则直接返回
     * </p>
     *
     * @param object
     * @return
     */
    public static Map<String, Object> fetchFieldAndValueKVMap(Object object) {
        if (object == null) {
            throw new NullPointerException("param object is null.");
        }
        if (object instanceof Map) {
            return (Map<String, Object>) object;
        }
        // 如果是基本数据类型抛出异常

        Map<String, Object> fieldValueMap = new HashMap<>();
        fetchFieldAndValueKVMap0(object.getClass(), object, fieldValueMap);

        return fieldValueMap;
    }

    private static void fetchFieldAndValueKVMap0(Class<?> clazz,
                                                 Object object,
                                                 Map<String, Object> fieldValueMap) {
        if (clazz == null) {
            return;
        }
        try {
            Method[] methods = clazz.getMethods();
            String methodName;
            Optional<String> fieldName;
            for (Method method : methods) {
                methodName = method.getName();
                if ("getClass".equals(methodName)) {
                    continue;
                }
                fieldName = StringUtil.getMethodNameToFiledName(methodName);
                if (method.getParameterCount() == 0
                        && method.getReturnType() != Void.class
                        && fieldName.isPresent()) {
                    if (!fieldValueMap.containsKey(fieldName.get())) {
                        fieldValueMap.put(fieldName.get(), method.invoke(object));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        fetchFieldAndValueKVMap0(clazz.getSuperclass(), object, fieldValueMap);
    }

}
