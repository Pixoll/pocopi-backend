package com.pocopi.api.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

public record Image(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String url,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String alt
) {
}
