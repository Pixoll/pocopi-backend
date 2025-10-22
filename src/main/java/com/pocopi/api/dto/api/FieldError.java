package com.pocopi.api.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;

public record FieldError(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String field,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message
) {
}
