package com.pocopi.api.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NewFormAnswers(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<NewFormAnswer> answers
) {
}
