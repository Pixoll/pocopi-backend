package com.pocopi.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserTestAttempt(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String attemptId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int groupId
) {
}
