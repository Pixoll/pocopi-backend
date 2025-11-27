package com.pocopi.api.dto.form;

import com.pocopi.api.models.form.FormModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FormUpdate(
    @Min(1)
    Integer id,

    @Size(min = FormModel.TITLE_MIN_LEN, max = FormModel.TITLE_MAX_LEN)
    String title,

    @NotNull
    @Size(max = 100)
    @Valid
    List<FormQuestionUpdate> questions
) {
}
