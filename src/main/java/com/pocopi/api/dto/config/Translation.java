package com.pocopi.api.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record Translation(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String key,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
    String value,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<String> arguments
) {
}
