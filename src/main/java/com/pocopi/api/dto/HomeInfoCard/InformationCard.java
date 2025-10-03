package com.pocopi.api.dto.HomeInfoCard;

import com.pocopi.api.dto.Image.Image;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record InformationCard(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String color,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Image> icon
) {
}
