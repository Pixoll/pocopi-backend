package com.pocopi.api.dto.TestGroup;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Optional;

public record PatchQuestion(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String text,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<PatchOption> options
) {
}
