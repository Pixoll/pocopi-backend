package com.pocopi.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AssignedTestPhase(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<AssignedTestQuestion> questions
) {
}
