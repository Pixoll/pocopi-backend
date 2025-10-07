package com.pocopi.api.dto.TimeLog;

import io.swagger.v3.oas.annotations.media.Schema;

public record SendOptionEvent(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int optionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String type
) {
}
