package com.pocopi.api.dto.results;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de una pregunta de test")
public record TestQuestionResult(
    @Schema(description = "ID de la pregunta")
    int questionId,

    @Schema(description = "ID de la fase")
    int phaseId,

    @Schema(description = "Timestamp de inicio")
    long startTimestamp,

    @Schema(description = "Timestamp de fin")
    long endTimestamp,

    @Schema(description = "¿Fue respondida correctamente?")
    boolean correct,

    @Schema(description = "¿La pregunta fue saltada?")
    boolean skipped,

    @Schema(description = "Cantidad de cambios de opción")
    int totalOptionChanges,

    @Schema(description = "Cantidad de hovers")
    int totalOptionHovers
) {
}
