package com.pocopi.api.dto.FormQuestionOption;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;

public record PatchFormOption(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<String> text
) {
}
