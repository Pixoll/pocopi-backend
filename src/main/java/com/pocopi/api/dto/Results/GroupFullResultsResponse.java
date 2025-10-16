package com.pocopi.api.dto.Results;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resultados completos (formularios y tests) de todos los usuarios de un grupo")
public record GroupFullResultsResponse(
        @Schema(description = "ID del grupo")
        int groupId,
        @Schema(description = "Resultados completos de los usuarios")
        List<UserAllResultsResponse> users
) {}