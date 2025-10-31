package com.pocopi.api.migration.entities.test;

import com.pocopi.api.migration.entities.OldConfigImage;

import java.util.ArrayList;

public record OldConfigTestQuestion(
    String text,
    OldConfigImage image,
    ArrayList<OldConfigTestOption> options,
    boolean randomizeOptions
) {
    public OldConfigTestQuestion {
        if ((text == null || text.trim().isEmpty()) && image == null) {
            throw new IllegalArgumentException(
                "Test question cannot have both text and image be null or empty at the same time"
            );
        }

        if (text != null && text.trim().isEmpty()) {
            text = null;
        }

        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Test question options list cannot be null or empty");
        }
    }
}
