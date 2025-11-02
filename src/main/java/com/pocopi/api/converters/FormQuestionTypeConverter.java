package com.pocopi.api.converters;

import com.pocopi.api.models.form.FormQuestionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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
        return name != null ? FormQuestionType.fromValue(name) : null;
    }
}
