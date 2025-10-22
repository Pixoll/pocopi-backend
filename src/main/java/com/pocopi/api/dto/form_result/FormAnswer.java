package com.pocopi.api.dto.form_result;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de un usuario a una pregunta del formulario.")
public record FormAnswer(
    @Schema(description = "ID de la pregunta", requiredMode = Schema.RequiredMode.REQUIRED)
    int questionId,

    @Schema(description = "Texto de la pregunta", requiredMode = Schema.RequiredMode.REQUIRED)
    String questionText,

    @Schema(
        description = "ID de la opcion seleccionada (si es pertinente))",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Integer optionId,

    @Schema(
        description = "Texto de la opcion seleccionada (si es pertinente)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String optionText,

    @Schema(
        description = "Valor numerico de la respuesta (si es pertinente)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Integer value,

    @Schema(
        description = "Respuesta en texto libre (si es pertinente)",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String answer
) {
}
