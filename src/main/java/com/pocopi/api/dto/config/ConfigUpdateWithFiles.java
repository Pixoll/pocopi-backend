package com.pocopi.api.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public record ConfigUpdateWithFiles(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "New application icon")
    MultipartFile icon,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Every image used in the information cards")
    Map<Integer, MultipartFile> informationCardImages,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Every image used in the pre-test form")
    Map<Integer, MultipartFile> preTestFormImages,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Every image used in the post-test form")
    Map<Integer, MultipartFile> postTestFormImages,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Every image used in the test groups")
    Map<Integer, MultipartFile> groupImages,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Last configuration updated")
    ConfigUpdate payload
) {
}
