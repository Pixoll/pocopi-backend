package com.pocopi.api.dto.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InformationCardUpdate(
    @Min(1)
    Integer id,

    @NotNull
    @Size(min = 1, max = 50)
    String title,

    @NotNull
    @Size(min = 1, max = 100)
    String description,

    @NotNull
    @Min(0x000000)
    @Max(0xffffff)
    Integer color
) {
}
