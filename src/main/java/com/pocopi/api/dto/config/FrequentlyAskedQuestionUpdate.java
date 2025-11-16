package com.pocopi.api.dto.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FrequentlyAskedQuestionUpdate(
    @Min(1)
    Integer id,

    @NotNull
    @Size(min = 1, max = 100)
    String question,

    @NotNull
    @Size(min = 1, max = 500)
    String answer
) {
}
