package com.pocopi.api.dto.TestResult;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resultados de test de todos los usuarios de un grupo")
public record GroupTestResultsResponse(
        @Schema(description = "ID del grupo")
        int groupId,
        @Schema(description = "Resultados individuales de los usuarios")
        List<UserTestResultsResponse> users
) {}
