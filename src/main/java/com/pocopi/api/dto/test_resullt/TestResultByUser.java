package com.pocopi.api.dto.test_resullt;

import com.pocopi.api.dto.user.UserBasicInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultados de test de un usuario")
public record TestResultByUser(
    @Schema(description = "Información del usuario, incluyendo el ID de grupo de test")
    UserBasicInfo user,

    @Schema(description = "Resultados por pregunta")
    List<TestQuestionResult> questions
) {
}
