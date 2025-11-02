package com.pocopi.api.dto.results;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultados de test de todos los usuarios de un grupo")
public record TestResultsByGroup(
    @Schema(description = "ID del grupo")
    int groupId,

    @Schema(description = "Resultados individuales de los usuarios")
    List<TestResultByUser> users
) {
}
