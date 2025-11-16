package com.pocopi.api.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;

public record Pattern(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String regex
) {
}
