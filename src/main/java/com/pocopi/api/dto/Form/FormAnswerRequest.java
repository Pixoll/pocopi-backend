package com.pocopi.api.dto.Form;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record FormAnswerRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int userId,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int formId,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<QuestionAnswer> answers
) {
    public static record QuestionAnswer(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            int questionId,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            Integer optionId,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            Integer value,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            String answer
    ) {}
}