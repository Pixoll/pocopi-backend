package com.pocopi.api.dto.results;

import com.pocopi.api.dto.event.QuestionEventLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TestResult(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long attemptId,

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
    double accuracy,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<QuestionEventLog> questionEvents
) {
}
