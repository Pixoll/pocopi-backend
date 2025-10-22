package com.pocopi.api.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ApiHttpError(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int code,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<FieldError> errors
) {
}
