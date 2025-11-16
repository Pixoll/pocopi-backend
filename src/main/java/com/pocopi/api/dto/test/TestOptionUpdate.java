package com.pocopi.api.dto.test;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TestOptionUpdate(
    @Min(1)
    Integer id,

    @Size(min = 1, max = 100)
    String text,

    @NotNull
    boolean correct
) {
}
