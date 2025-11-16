package com.pocopi.api.converters;

import com.pocopi.api.models.test.TestOptionEventType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TestOptionEventTypeJpaConverter implements AttributeConverter<TestOptionEventType, String> {
    @Override
    public String convertToDatabaseColumn(TestOptionEventType testOptionEventType) {
        if (testOptionEventType == null) {
            return null;
        }

        return testOptionEventType.getName();
    }

    @Override
    public TestOptionEventType convertToEntityAttribute(String name) {
        return name != null ? TestOptionEventType.fromValue(name) : null;
    }
}
