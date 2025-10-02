package com.pocopi.api.converters;

import com.pocopi.api.models.UserTestOptionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class UserTestOptionTypeConverter implements AttributeConverter<UserTestOptionType, String> {
    @Override
    public String convertToDatabaseColumn(UserTestOptionType userTestOptionType) {
        if (userTestOptionType == null) {
            return null;
        }

        return userTestOptionType.getName();
    }

    @Override
    public UserTestOptionType convertToEntityAttribute(String name) {
        if (name == null) {
            return null;
        }

        return Stream.of(UserTestOptionType.values())
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
