package com.pocopi.api.dto.event;

import com.pocopi.api.models.test.TestOptionEventType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NewOptionEventLog(
    @NotNull
    @Min(1)
    int optionId,

    @NotNull
    TestOptionEventType type,

    @NotNull
    @Min(0)
    long timestamp
) {
}
