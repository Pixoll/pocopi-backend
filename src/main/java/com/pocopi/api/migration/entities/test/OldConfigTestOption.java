package com.pocopi.api.migration.entities.test;

import com.pocopi.api.migration.entities.OldConfigImage;

public record OldConfigTestOption(
    String text,
    OldConfigImage image,
    boolean correct
) {
    public OldConfigTestOption {
        if ((text == null || text.trim().isEmpty()) && image == null) {
            throw new IllegalArgumentException(
                "Test option cannot have both text and image be null or empty at the same time"
            );
        }
    }
}
