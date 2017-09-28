package com.turding.sponge.core;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实体， 存储单元
 *
 * Created by yunfeng.pan on 17-6-19.
 */
public class Entity<T extends Storable> {

    /**
     * 实体对应类型
     */
    @Getter
    private Class<T> type;
    /**
     * 实体名称
     */
    @Getter
    @Setter
    private String name;
    /**
     * 对应存储目标
     */
    @Getter
    @Setter
    private String storeTarget;
    /**
     * 主键列表
     */
    @Getter
    private List<Field> pks;

//    @Getter
//    private List<Field> allFieldList;

    /**
     * 自增字段
     */
    @Getter
    private Field autoIncrementField;

    private Map<String, Field> fieldNameMapping;
    private Map<String, Field> storeNameMapping;

    public Entity(Class<T> type){
        this.type = type;
        pks = new ArrayList<>();
        //allFieldList = new ArrayList<>();
        fieldNameMapping = new LinkedHashMap<>();
        storeNameMapping = new LinkedHashMap<>();
    }

    public void addField(Field field) {
        //allFieldList.add(field);
        fieldNameMapping.put(field.getFieldName(), field);
        storeNameMapping.put(field.getStoreName(), field);

        if (field.isPk()) {
            pks.add(field);
        }

        if (field.isAutoIncrement()) {
            autoIncrementField = field;
        }
    }

    public boolean hasAutoIncrementField() {
        return autoIncrementField != null;
    }

    public boolean containsByFieldName(String fieldName) {
        return fieldNameMapping.containsKey(fieldName);
    }

    public boolean containsByStoreName(String storeName) {
        return storeNameMapping.containsKey(storeName);
    }

    public List<Field> getFields() {
        return new ArrayList<>(fieldNameMapping.values());
    }

    public List<Field> getStorableFields() {
        return fieldNameMapping.values().stream()
                .filter(Field::isStorable)
                .collect(Collectors.toList());
    }

    public List<Field> getSearchableFields() {
        return fieldNameMapping.values().stream()
                .filter(Field::isSearchable)
                .collect(Collectors.toList());
    }

    public List<Field> getUpdatableFields() {
        return fieldNameMapping.values().stream()
                .filter(Field::isUpdatable)
                .collect(Collectors.toList());
    }

    /**
     * 更具字段名获取存储名
     *
     * @param fieldName
     * @return
     */
    public String getFieldStoreNameByFieldName(String fieldName) {
        Field field = getFieldByFieldName(fieldName);
        if (field == null) {
            throw new IllegalArgumentException("the field [" + fieldName + "] not exsiting in " + type);
        }
        return field.getStoreName();
    }

    /**
     * 按字段名获取字段.
     *
     * @param fieldName
     * @return
     */
    public Field getFieldByFieldName(String fieldName) {
        return fieldNameMapping.get(fieldName);
    }

    /**
     * 按存储名获取字段.
     *
     * @param storeName
     * @return
     */
    public Field getFieldByStoreName(String storeName) {
        return storeNameMapping.get(storeName);
    }

    public static <T extends Storable> Entity of(Class<T> storeEntityType) {
        if (storeEntityType == null) {
            throw new NullPointerException("parse target entity type is null.");
        }
        Entity<T> entity = new Entity(storeEntityType);
        return entity;
    }

    public static <T extends Storable> Entity of(T storeEntity) {
        if (storeEntity == null) {
            throw new NullPointerException("parse target is null.");
        }
        Entity<T> entity = new Entity(storeEntity.getClass());
        return null;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    public static final class Field {
        /**
         * 字段类型.
         */
        @Getter
        private Class<?> type;

        /**
         * 程序中使用的名称.
         */
        @Getter
        private String fieldName;

        /**
         * 数据库中使用的名称.
         */
        @Getter
        private String storeName;

        /**
         * 值.
         */
        @Getter
        private Object value;

        /**
         * 主键
         */
        @Getter
        private boolean pk = false;

        /**
         * 自增
         */
        @Getter
        private boolean autoIncrement = false;

        /**
         * 字段是否可存储
         */
        @Getter
        private boolean storable = true;

        /**
         * 字段是否可更新.
         */
        @Getter
        private boolean updatable = true;

        /**
         * 可用于查询
         */
        @Getter
        private boolean searchable = true;
    }
}
