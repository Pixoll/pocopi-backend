package com.pocopi.api.dto.TestResult;

import com.pocopi.api.dto.Results.UserBasicInfoResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultados de test de un usuario")
public record UserTestResultsWithInfoResponse(
        @Schema(description = "Información del usuario, incluyendo el ID de grupo de test")
        UserBasicInfoResponse user,
        @Schema(description = "Resultados por pregunta")
        List<TestQuestionResult> questions
) {}