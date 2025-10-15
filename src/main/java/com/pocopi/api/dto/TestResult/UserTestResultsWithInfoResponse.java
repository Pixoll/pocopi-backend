package com.pocopi.api.dto.TestResult;

import com.pocopi.api.dto.Results.UserBasicInfoResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultados de test de un usuario")
public record UserTestResultsWithInfoResponse(
        @Schema(description = "ID del usuario")
        UserBasicInfoResponse user,
        @Schema(description = "Resultados por pregunta")
        List<TestQuestionResult> questions
) {}
