package com.pocopi.api.dto.results;

import com.pocopi.api.dto.event.QuestionTimestamp;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TestQuestionResult(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int questionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<QuestionTimestamp> timestamps,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean correct,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean skipped,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalOptionChanges,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalOptionHovers
) {
}
