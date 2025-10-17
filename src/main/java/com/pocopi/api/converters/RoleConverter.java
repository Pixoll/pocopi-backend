package com.pocopi.api.converters;

import com.pocopi.api.models.form.FormType;
import com.pocopi.api.models.user.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }

        return role.getName();
    }

    @Override
    public Role convertToEntityAttribute(String name) {
        if (name == null) {
            return null;
        }

        return Stream.of(Role.values())
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
