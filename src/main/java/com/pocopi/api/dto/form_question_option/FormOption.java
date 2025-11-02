package com.pocopi.api.dto.form_question_option;

import com.pocopi.api.dto.image.Image;
import io.swagger.v3.oas.annotations.media.Schema;

public record FormOption(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
    String text,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
    Image image
) {
}
