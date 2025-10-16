package com.pocopi.api.dto.FormResult;

import com.pocopi.api.dto.Results.UserBasicInfoResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuestas de los resultados de los formularios (pre-test y post-test) de un usuario")
public record UserFormWithInfoResultsResponse(
        @Schema(description = "Información del usuario, incluyendo el ID de grupo de test")
        UserBasicInfoResponse user,

        @Schema(description = "Resultados de formularios pre-test")
        List<FormAnswers> pre,

        @Schema(description = "Resultados de formularios post-test")
        List<FormAnswers> post
) {}