package com.pocopi.api.dto.TimeLog;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TimeLog(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int userId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int phaseId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int questionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long startTimestamp,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long endTimestamp,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean skipped,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean correct,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalOptionChanges,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalOptionHovers,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<Event> events
) {
}
