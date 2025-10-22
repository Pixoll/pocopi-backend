package com.pocopi.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Optional;

public record TestProtocolUpdate(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String label,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowPreviousPhase,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowPreviousQuestion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowSkipQuestion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<TestPhaseUpdate> phases
) {
}
