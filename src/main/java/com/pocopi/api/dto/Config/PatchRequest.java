package com.pocopi.api.dto.Config;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.File;
import java.util.List;
import java.util.Optional;

public record PatchRequest(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Icon by config")
    Optional<File> appIcon,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "All images from question and options each group")
    List<File> groupQuestionOptionsFiles,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "All images from question and options each phase")
    List<File> formQuestionOptionsFiles,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "All images from information cards")
    List<File> informationCardFiles,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Last configuration updated")
    PatchLastConfig updateLastConfig
) {
}
