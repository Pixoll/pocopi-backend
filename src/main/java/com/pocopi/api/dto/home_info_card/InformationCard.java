package com.pocopi.api.dto.home_info_card;

import com.pocopi.api.dto.image.Image;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record InformationCard(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int id,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int color,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Optional<Image> icon
) {
}
