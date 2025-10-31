package com.pocopi.api.migration.entities;

public record OldConfigTranslation(
    String key,
    String value
) {
    public OldConfigTranslation {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Translation key cannot be null or empty");
        }

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Translation value cannot be null or empty");
        }
    }
}
