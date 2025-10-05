package com.pocopi.api.dto.Form;

import com.pocopi.api.dto.FormQuestion.FormQuestion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record Form(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Form questions")
    List<FormQuestion> questions
) {
}
