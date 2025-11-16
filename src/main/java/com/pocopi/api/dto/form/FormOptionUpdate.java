package com.pocopi.api.dto.form;

import com.pocopi.api.models.form.FormQuestionOptionModel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record FormOptionUpdate(
    @Min(1)
    Integer id,

    @Size(min = FormQuestionOptionModel.TEXT_MIN_LEN, max = FormQuestionOptionModel.TEXT_MAX_LEN)
    String text
) {
}
