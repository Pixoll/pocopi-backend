package com.pocopi.api.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record UpdatedConfig(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> configUpdatesSummary,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> informationCardUpdatesSummary,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> faqUpdatedSummary,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> preTestUpdatedSummary,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> postTestUpdatedSummary,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> groupSummary
) {
}
