package com.pocopi.api.dto.TestGroup;

import com.pocopi.api.dto.Image.SingleImageResponse;

import java.util.List;

public record QuestionResponse(
    SingleImageResponse image,
    List<OptionResponse> options
) {
}
