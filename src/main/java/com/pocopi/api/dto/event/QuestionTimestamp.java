package com.pocopi.api.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;

public record QuestionTimestamp(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long start,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long end
) {
}
