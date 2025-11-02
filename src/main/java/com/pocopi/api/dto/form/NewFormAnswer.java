package com.pocopi.api.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;

public record NewFormAnswer(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int questionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer optionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer value,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String answer
) {
    public NewFormAnswer {
        if (answer != null && answer.trim().isEmpty()) {
            answer = null;
        }
    }
}
