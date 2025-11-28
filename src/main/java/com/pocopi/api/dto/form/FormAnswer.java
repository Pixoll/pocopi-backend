package com.pocopi.api.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;

public record FormAnswer(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int questionId,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer optionId,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer value,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String answer
) {
}
