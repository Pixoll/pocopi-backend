package com.pocopi.api.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;

public record NewQuestionEventLog(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer questionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long timestamp,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer duration
) {
}
