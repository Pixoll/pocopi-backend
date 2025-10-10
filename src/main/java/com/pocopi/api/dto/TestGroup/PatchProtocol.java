package com.pocopi.api.dto.TestGroup;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;
import java.util.List;

public record PatchProtocol(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowPreviousPhase,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowPreviousQuestion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean allowSkipQuestion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<PatchPhase> phases
) {
}
