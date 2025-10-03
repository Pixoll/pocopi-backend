package com.pocopi.api.dto.User;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record CreateUserRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "12.345.678-9", description = "Username by user like rut or other identifier")
        Optional<String> username,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int groupId,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        boolean anonymous,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int age,
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String password
) {
}
