package com.pocopi.api.converters;

import com.pocopi.api.models.test.UserTestOptionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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
        return name != null ? UserTestOptionType.fromValue(name) : null;
    }
}
