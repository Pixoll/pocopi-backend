package com.pocopi.api.dto.attempt;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TestAttemptsSummary(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double averageAccuracy,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double averageTimeTaken,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalQuestionsAnswered,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<TestAttemptSummary> users
) {
}
