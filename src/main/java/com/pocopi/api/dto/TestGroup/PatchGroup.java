package com.pocopi.api.dto.TestGroup;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record PatchGroup(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    int probability,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String label,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String greeting,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    PatchProtocol protocol
) {
}
