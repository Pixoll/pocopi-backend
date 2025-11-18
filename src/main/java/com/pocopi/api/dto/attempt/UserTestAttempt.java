package com.pocopi.api.dto.attempt;

import com.pocopi.api.dto.test.AssignedTestGroup;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UserTestAttempt(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean completedPreTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean completedTest,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean completedPostTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<UserTestAttemptAnswer> testAnswers,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    AssignedTestGroup assignedGroup
) {
}
