package com.pocopi.api.migration.entities.form;

import com.pocopi.api.models.form.FormType;

import java.util.ArrayList;

public record OldConfigForm(
    String title,
    FormType type,
    ArrayList<OldConfigFormQuestion> questions
) {
    public OldConfigForm {
        if (title != null && title.trim().isEmpty()) {
            title = null;
        }

        if (type == null) {
            throw new IllegalArgumentException("Form type cannot be null");
        }

        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("Form questions list cannot be null or empty");
        }
    }
}
