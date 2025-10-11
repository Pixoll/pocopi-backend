package com.pocopi.api.dto.HomeInfoCard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record PatchInformationCard(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Integer> id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int color
) {
}
