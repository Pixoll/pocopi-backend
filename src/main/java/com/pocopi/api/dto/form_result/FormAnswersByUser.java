package com.pocopi.api.dto.form_result;

import com.pocopi.api.dto.user.UserBasicInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuestas de los resultados de los formularios (pre-test y post-test) de un usuario")
public record FormAnswersByUser(
    @Schema(description = "Información del usuario, incluyendo el ID de grupo de test")
    UserBasicInfo user,

    @Schema(description = "Resultados de formularios pre-test")
    List<FormAnswers> pre,

    @Schema(description = "Resultados de formularios post-test")
    List<FormAnswers> post
) {
}
