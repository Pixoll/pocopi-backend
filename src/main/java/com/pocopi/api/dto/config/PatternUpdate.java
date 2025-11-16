package com.pocopi.api.dto.config;

import com.pocopi.api.models.config.PatternModel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PatternUpdate(
    @Min(1)
    Integer id,

    @NotNull
    @Size(min = PatternModel.NAME_MIN_LENGTH, max = PatternModel.NAME_MAX_LENGTH)
    String name,

    @NotNull
    @Size(min = PatternModel.REGEX_MIN_LENGTH, max = PatternModel.REGEX_MAX_LENGTH)
    String regex
) {
}
