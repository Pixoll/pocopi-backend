package com.pocopi.api.dto.Results;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos básicos de un usuario")
public record UserBasicInfoResponse(
        @Schema(description = "ID del usuario") int id,
        @Schema(description = "Nombre del usuario") String name,
        @Schema(description = "Email del usuario") String email,
        @Schema(description = "Edad del usuario") Integer age,
        @Schema(description = "ID del grupo al que pertenece el usuario") int groupId
) {}