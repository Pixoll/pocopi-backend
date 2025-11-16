package com.pocopi.api.dto.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewFormAnswer(
    @NotNull
    @Min(1)
    int questionId,

    @Min(1)
    Integer optionId,

    @Min(0x0000)
    @Max(0xffff)
    Integer value,

    @Size(min = 1, max = 1000)
    String answer
) {
}
