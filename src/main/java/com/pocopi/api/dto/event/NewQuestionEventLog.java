package com.pocopi.api.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NewQuestionEventLog(
    @NotNull
    @Min(1)
    int questionId,

    @NotNull
    @Min(0)
    long timestamp,

    @NotNull
    @Min(0)
    int duration
) {
}
