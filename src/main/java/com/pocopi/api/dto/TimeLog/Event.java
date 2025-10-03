package com.pocopi.api.dto.TimeLog;

import io.swagger.v3.oas.annotations.media.Schema;

public record Event(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String type,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int optionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long timestamp
) {
}
