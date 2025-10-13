package com.pocopi.api.converters;

import com.pocopi.api.models.form.FormQuestionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class FormQuestionTypeConverter implements AttributeConverter<FormQuestionType, String> {
    @Override
    public String convertToDatabaseColumn(FormQuestionType formQuestionType) {
        if (formQuestionType == null) {
            return null;
        }

        return formQuestionType.getName();
    }

    @Override
    public FormQuestionType convertToEntityAttribute(String name) {
        if (name == null) {
            return null;
        }

        return Stream.of(FormQuestionType.values())
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
