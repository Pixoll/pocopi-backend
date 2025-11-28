package com.pocopi.api.dto.results;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TestResultsByConfig(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int configVersion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<TestResult> attemptsResults
) {
}
