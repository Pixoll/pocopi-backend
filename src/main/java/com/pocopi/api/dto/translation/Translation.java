package com.pocopi.api.dto.translation;

import io.swagger.v3.oas.annotations.media.Schema;

public record Translation(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String key,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String value
) {
}
