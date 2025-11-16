package com.pocopi.api.dto.form;

import com.pocopi.api.models.form.UserFormAnswerModel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewFormAnswer(
    @NotNull
    @Min(1)
    int questionId,

    @Min(1)
    Integer optionId,

    @Min(0x0000)
    @Max(0xffff)
    Integer value,

    @Size(min = UserFormAnswerModel.ANSWER_MIN_LEN, max = UserFormAnswerModel.ANSWER_MAX_LEN)
    String answer
) {
}
