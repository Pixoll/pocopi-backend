package com.pocopi.api.dto.TestGroup;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record Protocol(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<Phase> phases
) {
}
