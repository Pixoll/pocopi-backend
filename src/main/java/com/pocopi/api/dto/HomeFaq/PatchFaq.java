package com.pocopi.api.dto.HomeFaq;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record PatchFaq(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String question,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String answer
) {
}
