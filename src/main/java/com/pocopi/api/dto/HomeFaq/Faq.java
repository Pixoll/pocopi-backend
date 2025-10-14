package com.pocopi.api.dto.HomeFaq;

import io.swagger.v3.oas.annotations.media.Schema;

public record Faq(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String question,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String answer
) {
}
