package com.pocopi.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record Admin(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String username
) {
}
