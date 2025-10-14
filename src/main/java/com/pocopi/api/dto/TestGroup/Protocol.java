package com.pocopi.api.dto.TestGroup;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record Protocol(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String label,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowPreviousPhase,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowPreviousQuestion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowSkipQuestion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<Phase> phases
) {
}
