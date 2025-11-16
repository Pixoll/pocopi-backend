package com.pocopi.api.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FormUpdate(
    @Min(1)
    Integer id,

    @Size(min = 1, max = 100)
    String title,

    @NotNull
    @Valid
    List<FormQuestionUpdate> questions
) {
}
