package com.pocopi.api.dto.HomeInfoCard;
import io.swagger.v3.oas.annotations.media.Schema;

public record PatchInformationCard(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int color
) {
}
