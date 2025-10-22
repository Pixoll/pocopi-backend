package com.pocopi.api.dto.home_faq;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record FrequentlyAskedQuestionUpdate(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String question,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String answer
) {
}
