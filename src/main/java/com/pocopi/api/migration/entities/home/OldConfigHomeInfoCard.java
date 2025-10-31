package com.pocopi.api.migration.entities.home;

import com.pocopi.api.migration.entities.OldConfigImage;

public record OldConfigHomeInfoCard(
    String title,
    String description,
    OldConfigImage icon,
    Integer color
) {
    public OldConfigHomeInfoCard {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Home information card title cannot be null or empty");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Home information card description cannot be null or empty");
        }

        if (color != null && (color < 0 || color > 0xffffff)) {
            throw new IllegalArgumentException("Home information card color must be between #000000 and #ffffff");
        }
    }
}
