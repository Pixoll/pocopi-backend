package com.pocopi.api.dto.TestGroup;

import com.pocopi.api.dto.Image.Image;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record Question(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Image image,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<Option> options
) {
}
