package com.pocopi.api.dto.FormQuestion;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pocopi.api.dto.Image.SingleImageResponse;

import java.util.Optional;

public record BaseQuestion(
        int id,
        String category,
        Optional<String> text,
        Optional<SingleImageResponse> image
) {
}
