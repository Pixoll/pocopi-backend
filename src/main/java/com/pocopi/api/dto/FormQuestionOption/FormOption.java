package com.pocopi.api.dto.FormQuestionOption;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pocopi.api.dto.Image.SingleImageResponse;

import java.util.Optional;

public record FormOption(
    Optional<String> text,
    Optional<SingleImageResponse> image
) {
}
