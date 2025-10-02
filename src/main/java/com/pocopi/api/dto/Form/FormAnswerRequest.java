package com.pocopi.api.dto.Form;

import java.util.List;

public record FormAnswerRequest(
        int userId,
        int formId,
        List<QuestionAnswer> answers
) {
    public static record QuestionAnswer(
            int questionId,
            Integer optionId,
            Integer value,
            String answer
    ) {}
}