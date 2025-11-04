package com.pocopi.api.dto.event;

import com.pocopi.api.models.test.TestOptionEventType;
import io.swagger.v3.oas.annotations.media.Schema;

public record NewOptionEventLog(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer optionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    TestOptionEventType type,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long timestamp
) {
}
