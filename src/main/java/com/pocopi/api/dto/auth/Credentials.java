package com.pocopi.api.dto.auth;

import jakarta.validation.constraints.NotNull;

public record Credentials(
    @NotNull
    String username,

    @NotNull
    String password
) {
}
