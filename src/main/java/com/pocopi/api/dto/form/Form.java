package com.pocopi.api.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record Form(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<FormQuestion> questions
) {
}
