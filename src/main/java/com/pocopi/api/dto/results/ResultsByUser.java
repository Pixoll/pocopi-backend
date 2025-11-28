package com.pocopi.api.dto.results;

import com.pocopi.api.dto.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ResultsByUser(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    User user,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<ResultsByConfig> results
) {
}
