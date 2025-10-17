package com.pocopi.api.dto.User;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserSummary(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int age,

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
