package com.pluxity.jpa.domain;

import java.util.Arrays;

public interface Updatable<T> {

    default void update(T dto){
        update(this, dto);
    }

    static <T> void update(Object target, T dto){
        Class<?> clazz = target.getClass();
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            try{
                Object value = field.get(dto);
                if(value != null){
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(
                        String.format("Cannot update field: %s of class: %s with value: %s",
                                field.getName(), clazz.getName(), dto), e);
            }
        });
    }
}
