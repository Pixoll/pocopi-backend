package com.pocopi.api.dto.event;

import com.pocopi.api.models.test.TestOptionEventType;
import io.swagger.v3.oas.annotations.media.Schema;

public record OptionEventLog(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int optionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    TestOptionEventType type,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long timestamp
) {
}
