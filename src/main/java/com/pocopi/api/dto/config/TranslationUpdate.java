package com.pocopi.api.dto.config;

import com.pocopi.api.models.config.TranslationKeyModel;
import com.pocopi.api.models.config.TranslationValueModel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TranslationUpdate(
    @NotNull
    @Size(min = TranslationKeyModel.KEY_MIN_LENGTH, max = TranslationKeyModel.KEY_MAX_LENGTH)
    String key,

    @NotNull
    @Size(min = TranslationValueModel.VALUE_MIN_LENGTH, max = TranslationValueModel.VALUE_MAX_LENGTH)
    String value
) {
}
