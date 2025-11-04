package com.pocopi.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserTestAttempt(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean completedPreTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int lastTestQuestionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    AssignedTestGroup assignedGroup
) {
}
