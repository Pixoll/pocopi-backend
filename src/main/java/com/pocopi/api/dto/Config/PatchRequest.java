package com.pocopi.api.dto.Config;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

public record PatchRequest(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Icon by config")
    Optional<MultipartFile> appIcon,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "All images from question and options by pre test form")
    Map<Integer, MultipartFile> preTestFormQuestionOptionsFiles,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "All images from question and options by post test form")
    Map<Integer, MultipartFile> postTestFormQuestionOptionsFiles,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "All images from question and options each phase and protocol")
    Map<Integer, MultipartFile> groupQuestionOptionsFiles,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "All images from information cards")
    Map<Integer, MultipartFile> informationCardFiles,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Last configuration updated")
    PatchLastConfig updateLastConfig
) {
}