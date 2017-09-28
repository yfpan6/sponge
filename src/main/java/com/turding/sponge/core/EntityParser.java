package com.turding.sponge.core;

import com.turding.sponge.annotation.*;
import com.turding.sponge.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 实体解析, 解析出实体中标注的StoreFieldBean信息
 *
 * Created by yunfeng.pan on 17-6-15.
 */
public class EntityParser<T extends Storable> {

    private Entity<T> entity;
    private T storeBean;
    private Class<T> storeEntityType;
    private ParsedEntityParser parsedEntityParser;

    private EntityParser(Class<T> storeEntityType) {
        this.storeEntityType = storeEntityType;
        this.parsedEntityParser = new ParsedEntityParser();
    }

    private EntityParser(T storeBean) {
        this.storeEntityType = (Class<T>) storeBean.getClass();
        this.storeBean = storeBean;
        this.parsedEntityParser = new ParsedEntityParser();
    }

    public static <T extends Storable> EntityParser<T> of(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("the param clazz is null.");
        }
        EntityParser<T> parser = new EntityParser(clazz);
        return parser;
    }

    public static <T extends Storable> EntityParser<T> of(T storeBean) {
        if (storeBean == null) {
            throw new NullPointerException("the param storeBean is null.");
        }
        EntityParser<T> parser = new EntityParser(storeBean);
        return parser;
    }

    public ParsedEntityParser parse() {
        if (!isValidEntity()) {
            throw new IllegalArgumentException("parsed param object is not a jpa entity.");
        }

        try {
            entity = new Entity<>(storeEntityType);
            parseStoreTarget();
            parseFileds(storeEntityType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return parsedEntityParser;
    }

    private boolean isValidEntity() {
        if (storeEntityType.getAnnotation(StoreEntity.class) == null) {
            return false;
        }

        return true;
    }

    private void parseStoreTarget() {
        StoreEntity storeEntity = storeEntityType.getAnnotation(StoreEntity.class);
        String storeTarget = storeEntity.storeTarget();
        if (StringUtil.isBlank(storeTarget)) {
            storeTarget = StringUtil.toSnake(storeEntityType.getSimpleName());
        }
        entity.setStoreTarget(storeTarget);
    }

    /**
     * 解析列.
     * <p>默认扫描字段上的注解, 不支持get方法注解. 子类字段会覆盖父类同名字段的配置.</p>
     */
    private void parseFileds(Class<?> clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        StoreField storeField;
        String storeName, fieldName;
        boolean storable, updatable, searchable;
        Entity.Field entityField;
        for (Field field : fields) {
            fieldName = field.getName();
            if (Modifier.isStatic(field.getModifiers())
                    || Modifier.isFinal(field.getModifiers())
                    || entity.containsByFieldName(fieldName)) {
                continue;
            }

            storeField = field.getAnnotation(StoreField.class);
            storeName = storeField == null ? null : storeField.storeName();
            storeName = StringUtil.isBlank(storeName) ? fieldName : storeName;
            updatable = storeField == null ? true : storeField.updatable();
            storable = searchable = true;
            Transient tran = field.getAnnotation(Transient.class);
            if (tran != null) {
                storable = false;
                updatable = false;
                searchable = tran.searchable();
                storeName = StringUtil.isBlank(tran.searchName()) ? storeName : tran.searchName();
            }

            field.setAccessible(true);
            boolean isPk = field.isAnnotationPresent(PK.class);
            entityField = new Entity.Field(field.getType(), fieldName, storeName,
                    storeBean == null ? null : field.get(storeBean) ,
                    isPk,
                    field.isAnnotationPresent(AutoIncrement.class),
                    storable, updatable && !isPk, searchable);
            entity.addField(entityField);
        }

        Class superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            parseFileds(superClazz);
        }
    }

    public class ParsedEntityParser {

        public Entity<T> result() {
            return entity;
        }

    }
}
