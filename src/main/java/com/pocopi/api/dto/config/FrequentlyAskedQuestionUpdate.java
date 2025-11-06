package com.pocopi.api.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;

public record FrequentlyAskedQuestionUpdate(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String question,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String answer
) {
}
