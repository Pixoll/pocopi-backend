package com.pocopi.api.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;

public record OptionSelectionEvent(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int optionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long timestamp,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int x,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int y
) {
}
