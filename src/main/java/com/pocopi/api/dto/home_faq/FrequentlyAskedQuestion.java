package com.pocopi.api.dto.home_faq;

import io.swagger.v3.oas.annotations.media.Schema;

public record FrequentlyAskedQuestion(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String question,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String answer
) {
}
