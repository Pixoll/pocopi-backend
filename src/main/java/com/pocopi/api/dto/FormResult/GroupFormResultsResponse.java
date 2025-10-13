package com.pocopi.api.dto.FormResult;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resultados de formularios de todos los usuarios de un grupo.")
public record GroupFormResultsResponse(
        @Schema(description = "ID del grupo", requiredMode = Schema.RequiredMode.REQUIRED)
        int groupId,

        @Schema(description = "Resultados de formularios de los usuarios del grupo", requiredMode = Schema.RequiredMode.REQUIRED)
        List<UserFormResultsResponse> users
) {}