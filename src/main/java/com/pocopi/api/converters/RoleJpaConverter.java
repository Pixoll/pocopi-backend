package com.pocopi.api.converters;

import com.pocopi.api.models.user.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleJpaConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }

        return role.getName();
    }

    @Override
    public Role convertToEntityAttribute(String name) {
        return name != null ? Role.fromValue(name) : null;
    }
}
