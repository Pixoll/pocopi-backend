package com.pocopi.api.converters;

import com.pocopi.api.models.FormType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class FormTypeConverter implements AttributeConverter<FormType, String> {
    @Override
    public String convertToDatabaseColumn(FormType formType) {
        if (formType == null) {
            return null;
        }

        return formType.getName();
    }

    @Override
    public FormType convertToEntityAttribute(String name) {
        if (name == null) {
            return null;
        }

        return Stream.of(FormType.values())
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
