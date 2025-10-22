package com.pocopi.api.dto.test;

import com.pocopi.api.dto.image.Image;
import io.swagger.v3.oas.annotations.media.Schema;

public record TestOption(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String text,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Image image,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean correct
) {
}
