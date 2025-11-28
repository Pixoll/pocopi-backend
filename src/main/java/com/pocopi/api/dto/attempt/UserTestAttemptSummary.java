package com.pocopi.api.dto.attempt;

import com.pocopi.api.dto.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserTestAttemptSummary(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    User user,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int configVersion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String group,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long timestamp,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int timeTaken,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int correctQuestions,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int questionsAnswered,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double accuracy
) {
}
