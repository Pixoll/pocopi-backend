package com.pocopi.api.dto.Results;

import com.pocopi.api.dto.FormResult.FormAnswers;
import com.pocopi.api.dto.TestResult.TestQuestionResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultados completos (formularios y tests) de un usuario, con info de usuario unica")
public record UserAllResultsResponse(
        @Schema(description = "Datos del usuario")
        UserBasicInfoResponse user,

        @Schema(description = "Resultados de formularios")
        UserFormsPart forms,

        @Schema(description = "Resultados de test")
        UserTestsPart tests
) {
    public record UserFormsPart(
            List<FormAnswers> pre,
            List<FormAnswers> post
    ) {}

    public record UserTestsPart(
            List<TestQuestionResult> questions
    ) {}
}