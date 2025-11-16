package com.pocopi.api.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NewFormAnswers(
    @NotNull
    @Valid
    List<NewFormAnswer> answers
) {
}
