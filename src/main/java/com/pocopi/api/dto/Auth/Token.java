package com.pocopi.api.dto.Auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record Token(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String token
) {
}
