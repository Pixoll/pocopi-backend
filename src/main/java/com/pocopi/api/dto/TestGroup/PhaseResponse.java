package com.pocopi.api.dto.TestGroup;

import java.util.List;

public record PhaseResponse(
    List<QuestionResponse> questions
) {
}
