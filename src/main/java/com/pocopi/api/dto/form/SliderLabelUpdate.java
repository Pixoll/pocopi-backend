package com.pocopi.api.dto.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SliderLabelUpdate(
    @Min(1)
    Integer id,

    @NotNull
    @Min(0x0000)
    @Max(0xffff)
    int number,

    @NotNull
    @Size(min = 1, max = 50)
    String label
) {
}
