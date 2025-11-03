package com.pocopi.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AssignedTestGroup(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String label,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
    String greeting,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowPreviousPhase,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowPreviousQuestion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowSkipQuestion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<AssignedTestPhase> phases
) {
}
