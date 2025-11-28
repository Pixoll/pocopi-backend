package com.pocopi.api.dto.results;

import com.pocopi.api.dto.form.FormAnswer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record FormAnswersByConfig(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int configVersion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<FormAnswer> preTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<FormAnswer> postTestForm
) {
}
