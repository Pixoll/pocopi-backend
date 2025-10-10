package com.pocopi.api.dto.Form;

import com.pocopi.api.dto.FormQuestion.PatchFormQuestion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Optional;

public record PatchForm(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Form questions")
    List<PatchFormQuestion> questions
) {
}
