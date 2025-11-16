package com.pocopi.api.converters;

import com.pocopi.api.models.form.FormType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class FormTypeSpringConverter implements Converter<String, FormType> {
    @Override
    public FormType convert(@NonNull String source) {
        return FormType.fromValue(source);
    }
}
