package com.pocopi.api.migration.entities.form;

public record OldConfigFormSliderLabel(
    int number,
    String label
) {
    public OldConfigFormSliderLabel {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Test group label cannot be null or empty");
        }
    }
}
