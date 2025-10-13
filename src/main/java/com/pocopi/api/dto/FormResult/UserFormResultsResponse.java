package com.pocopi.api.dto.FormResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuestas de los resultados de los formularios(pre-test y post-test)")
public record UserFormResultsResponse(
        @Schema(description = "ID del usuario", requiredMode = Schema.RequiredMode.REQUIRED)
        int userId,

        @Schema(description = "Resultados de formularios pre-test", requiredMode = Schema.RequiredMode.REQUIRED)
        List<FormAnswers> pre,

        @Schema(description = "Resultados de formularios post-test", requiredMode = Schema.RequiredMode.REQUIRED)
        List<FormAnswers> post
) {}