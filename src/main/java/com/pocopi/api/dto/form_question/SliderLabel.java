package com.pocopi.api.dto.form_question;

import io.swagger.v3.oas.annotations.media.Schema;

public record SliderLabel(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int number,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String label
) {
}
