package com.pluxity.jpa.domain;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public interface Mappable<E, R> {
    default R toResponseDto(E entity) {
        R dto = null;
        try {
            dto = (R) entity.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        if (dto == null) {
            return null;
        }

        Class<?> clazz = entity.getClass();
        R resultDto = dto;
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {
                    field.set(resultDto, value);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(
                        String.format("Cannot map field: %s of class: %s",
                                field.getName(), clazz.getName()), e);
            }
        });
        return resultDto;
    }
}
