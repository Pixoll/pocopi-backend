package com.pocopi.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record User(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean anonymous,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String name,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String email,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer age
) {
}
