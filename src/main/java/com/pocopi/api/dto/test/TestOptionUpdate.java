package com.pocopi.api.dto.test;

import com.pocopi.api.models.test.TestOptionModel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TestOptionUpdate(
    @Min(1)
    Integer id,

    @Size(min = TestOptionModel.TEXT_MIN_LEN, max = TestOptionModel.TEXT_MAX_LEN)
    String text,

    @NotNull
    boolean correct
) {
}
