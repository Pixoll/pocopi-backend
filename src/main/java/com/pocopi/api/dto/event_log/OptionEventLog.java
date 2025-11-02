package com.pocopi.api.dto.event_log;

import io.swagger.v3.oas.annotations.media.Schema;

public record OptionEventLog(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String type,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int optionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long timestamp
) {
}
