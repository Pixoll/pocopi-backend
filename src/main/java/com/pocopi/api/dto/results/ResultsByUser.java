package com.pocopi.api.dto.results;

import com.pocopi.api.dto.form.FormAnswers;
import com.pocopi.api.dto.user.UserBasicInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultados completos (formularios y tests) de un usuario, con info de usuario unica")
public record ResultsByUser(
    @Schema(description = "Datos del usuario")
    UserBasicInfo user,

    @Schema(description = "Resultados de formularios")
    UserFormsResult forms,

    @Schema(description = "Resultados de test")
    UserTestsResult tests
) {
    public record UserFormsResult(
        List<FormAnswers> pre,
        List<FormAnswers> post
    ) {
    }

    public record UserTestsResult(
        List<TestQuestionResult> questions
    ) {
    }
}
