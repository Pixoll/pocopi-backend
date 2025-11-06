package com.pocopi.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TestPhaseUpdate(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean randomizeQuestions,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<TestQuestionUpdate> questions
) {
}
