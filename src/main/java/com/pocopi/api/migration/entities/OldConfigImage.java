package com.pocopi.api.migration.entities;

import java.nio.file.Path;

public record OldConfigImage(
    Path relativePath,
    Path absolutePath,
    String alt
) {
    public OldConfigImage {
        if (relativePath == null
            || absolutePath == null
            || relativePath.toString().trim().isEmpty()
            || absolutePath.toString().trim().isEmpty()
        ) {
            throw new IllegalArgumentException("Image path cannot be null or empty");
        }

        if (alt == null || alt.trim().isEmpty()) {
            throw new IllegalArgumentException("Image alt cannot be null or empty");
        }
    }
}
