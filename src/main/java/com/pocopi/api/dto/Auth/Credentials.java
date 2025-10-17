package com.pocopi.api.dto.Auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record Credentials(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {
}
