package com.hangout.core.auth_service.entity.converter;

import java.util.stream.Stream;

import com.hangout.core.auth_service.entity.Action;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActionConverter implements AttributeConverter<Action, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Action action) {
        if (action == null) {
            return null;
        }
        return action.getCode();
    }

    @Override
    public Action convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return Stream.of(Action.values())
                .filter(a -> a.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

    }

}
