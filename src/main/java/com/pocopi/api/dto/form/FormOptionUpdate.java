package com.pocopi.api.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;

public record FormOptionUpdate(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String text
) {
}
