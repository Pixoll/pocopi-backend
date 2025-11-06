package com.pocopi.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;

public record TestOptionUpdate(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String text,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean correct
) {
}
