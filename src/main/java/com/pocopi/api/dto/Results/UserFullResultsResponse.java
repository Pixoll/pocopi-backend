package com.pocopi.api.dto.Results;

import com.pocopi.api.dto.FormResult.UserFormWithInfoResultsResponse;
import com.pocopi.api.dto.TestResult.UserTestResultsWithInfoResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Todos los resultados (formularios y tests) de un usuario")
public record UserFullResultsResponse(
        @Schema(description = "Resultados de formularios")
        UserFormWithInfoResultsResponse forms,

        @Schema(description = "Resultados de test")
        UserTestResultsWithInfoResponse tests
) {}
