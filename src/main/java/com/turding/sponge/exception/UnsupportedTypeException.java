package com.turding.sponge.exception;

/**
 * Created by yunfeng.pan on 17-6-20.
 */
public class UnsupportedTypeException extends RuntimeException {
    public UnsupportedTypeException(Class<?> idType) {
        super(idType.getName());
    }

    public static UnsupportedTypeException create(Class<?> idType) {
        return new UnsupportedTypeException(idType);
    }
}
