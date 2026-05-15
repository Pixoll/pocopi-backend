package com.pocopi.api.dto.event;

import com.pocopi.api.models.test.TestOptionEventType;
import com.pocopi.api.models.test.UserTestOptionLogModel;
import jakarta.validation.constraints.Max;
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
    long timestamp,

    @NotNull
    @Min(UserTestOptionLogModel.COORD_MIN)
    @Max(UserTestOptionLogModel.COORD_MAX)
    int x,

    @NotNull
    @Min(UserTestOptionLogModel.COORD_MIN)
    @Max(UserTestOptionLogModel.COORD_MAX)
    int y
) {
}
