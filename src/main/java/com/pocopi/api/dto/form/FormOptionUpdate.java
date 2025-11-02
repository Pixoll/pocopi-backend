package com.pocopi.api.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record FormOptionUpdate(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<String> text
) {
}
