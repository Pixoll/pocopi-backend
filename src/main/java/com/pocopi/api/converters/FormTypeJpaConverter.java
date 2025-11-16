package com.pocopi.api.converters;

import com.pocopi.api.models.form.FormType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FormTypeJpaConverter implements AttributeConverter<FormType, String> {
    @Override
    public String convertToDatabaseColumn(FormType formType) {
        if (formType == null) {
            return null;
        }

        return formType.getName();
    }

    @Override
    public FormType convertToEntityAttribute(String name) {
        return name != null ? FormType.fromValue(name) : null;
    }
}
