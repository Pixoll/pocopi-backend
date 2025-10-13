package com.pocopi.api.dto.TestGroup;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record PatchOption(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String text,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean correct
) {
}
