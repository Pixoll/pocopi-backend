package com.pocopi.api.migration.entities.form;

import com.pocopi.api.migration.entities.OldConfigImage;

public record OldConfigFormOption(
    String text,
    OldConfigImage image
) {
    public OldConfigFormOption {
        if ((text == null || text.trim().isEmpty()) && image == null) {
            throw new IllegalArgumentException(
                "Form option cannot have both text and image be null or empty at the same time"
            );
        }
    }
}
