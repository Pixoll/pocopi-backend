package com.pocopi.api.dto.time_log;

import io.swagger.v3.oas.annotations.media.Schema;

public record NewTimeLogEvent(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int optionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String type
) {
}
