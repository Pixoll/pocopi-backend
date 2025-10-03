package com.pocopi.api.dto.TestGroup;

import io.swagger.v3.oas.annotations.media.Schema;

public record Option(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String text,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean correct
) {
}
