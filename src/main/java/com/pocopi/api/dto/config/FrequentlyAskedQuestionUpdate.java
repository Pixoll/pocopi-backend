package com.pocopi.api.dto.config;

import com.pocopi.api.models.config.HomeFaqModel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FrequentlyAskedQuestionUpdate(
    @Min(1)
    Integer id,

    @NotNull
    @Size(min = HomeFaqModel.QUESTION_MIN_LEN, max = HomeFaqModel.QUESTION_MAX_LEN)
    String question,

    @NotNull
    @Size(min = HomeFaqModel.ANSWER_MIN_LEN, max = HomeFaqModel.ANSWER_MAX_LEN)
    String answer
) {
}
