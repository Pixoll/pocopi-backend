package com.pocopi.api.dto.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TestQuestionUpdate(
    @Min(1)
    Integer id,

    @Size(min = 1, max = 100)
    String text,

    @NotNull
    boolean randomizeOptions,

    @NotNull
    @Valid
    List<TestOptionUpdate> options
) {
    public TestQuestionUpdate {
        if (text != null && text.isEmpty()) {
            text = null;
        }
    }
}
