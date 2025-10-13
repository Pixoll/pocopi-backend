package com.pocopi.api.dto.TestResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultados de test de un usuario")
public record UserTestResultsResponse(
        @Schema(description = "ID del usuario")
        int userId,
        @Schema(description = "Resultados por pregunta")
        List<TestQuestionResult> questions
) {}
