package com.pocopi.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record NewUser(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean anonymous,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Byte age,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {
}
