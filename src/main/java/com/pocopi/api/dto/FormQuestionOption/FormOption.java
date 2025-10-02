package com.pocopi.api.dto.FormQuestionOption;

import com.pocopi.api.dto.Image.SingleImageResponse;

import java.util.Optional;

public record FormOption(
    int id,
    Optional<String> text,
    Optional<SingleImageResponse> image
) {
}
