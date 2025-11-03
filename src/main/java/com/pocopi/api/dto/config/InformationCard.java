package com.pocopi.api.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;

public record InformationCard(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"integer", "null"})
    Integer color,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
    Image icon
) {
}
