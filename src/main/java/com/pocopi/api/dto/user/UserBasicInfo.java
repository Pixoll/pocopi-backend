package com.pocopi.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos básicos de un usuario")
public record UserBasicInfo(
    @Schema(description = "ID del usuario")
    int id,

    @Schema(description = "Nombre del usuario")
    String name,

    @Schema(description = "Email del usuario")
    String email,

    @Schema(description = "Edad del usuario")
    Integer age
) {
}
