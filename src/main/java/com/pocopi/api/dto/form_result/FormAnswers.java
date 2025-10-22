package com.pocopi.api.dto.form_result;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Contiene las respuestas a un formulario especifico.")
public record FormAnswers(
    @Schema(description = "ID del formulario", requiredMode = Schema.RequiredMode.REQUIRED)
    int formId,

    @Schema(description = "Título del formulario", requiredMode = Schema.RequiredMode.REQUIRED)
    String formTitle,

    @Schema(description = "Respuestas a preguntas del formulario", requiredMode = Schema.RequiredMode.REQUIRED)
    List<FormAnswer> answers
) {
}
