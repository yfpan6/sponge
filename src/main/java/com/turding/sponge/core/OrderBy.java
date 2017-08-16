package com.turding.sponge.core;

import lombok.AllArgsConstructor;

/**
 * 排序表达式
 *
 * Created by yunfeng.pan on 17-6-16.
 */
@AllArgsConstructor
public abstract class OrderBy {

    protected String fieldName;

    public String fieldName() {
        return fieldName;
    }

    public static OrderByBuilder of(String fieldName) {
        return new OrderByBuilder(fieldName);
    }

    public static class Asc extends OrderBy {
        public Asc(String fieldName) {
            super(fieldName);
        }
    }

    public static class Desc extends OrderBy {
        public Desc(String fieldName) {
            super(fieldName);
        }
    }

    public static class OrderByBuilder {
        protected String fieldName;

        private OrderByBuilder(String fieldName) {
            this.fieldName = fieldName;
        }

        public Asc asc() {
            return new Asc(fieldName);
        }

        public Desc desc() {
            return new Desc(fieldName);
        }
    }
}
