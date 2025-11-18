package com.pocopi.api.dto.attempt;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserTestAttemptAnswer(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int questionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int optionId
) {
}
