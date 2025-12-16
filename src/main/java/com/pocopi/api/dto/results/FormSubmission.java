package com.pocopi.api.dto.results;

import com.pocopi.api.dto.form.FormAnswer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record FormSubmission(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long attemptId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long timestamp,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<FormAnswer> answers
) {
}
