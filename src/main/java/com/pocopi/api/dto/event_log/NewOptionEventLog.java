package com.pocopi.api.dto.event_log;

import com.pocopi.api.models.test.UserTestOptionType;
import io.swagger.v3.oas.annotations.media.Schema;

public record NewOptionEventLog(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer optionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    UserTestOptionType type,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long timestamp
) {
}
