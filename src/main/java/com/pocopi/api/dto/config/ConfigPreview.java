package com.pocopi.api.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;

public record ConfigPreview(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int version,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
    Image icon,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
    String subtitle,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean active,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean canDelete
) {
}
