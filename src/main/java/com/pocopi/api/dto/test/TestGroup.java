package com.pocopi.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;

public record TestGroup(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Probability between 0 and 100")
    int probability,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String label,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String greeting,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    TestProtocol protocol
) {
}
