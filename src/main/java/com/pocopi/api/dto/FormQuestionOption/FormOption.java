package com.pocopi.api.dto.FormQuestionOption;

import com.pocopi.api.dto.Image.Image;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record FormOption(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<String> text,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Image> image
) {
}
