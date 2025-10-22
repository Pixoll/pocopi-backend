package com.pocopi.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UsersSummary(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double averageAccuracy,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    double averageTimeTaken,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalQuestionsAnswered,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<UserSummary> users
) {
}
