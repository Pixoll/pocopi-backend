package com.pocopi.api.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record FormUpdate(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Form questions")
    List<FormQuestionUpdate> questions
) {
}
