package com.pocopi.api.dto.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TestPhaseUpdate(
    @Min(1)
    Integer id,

    @NotNull
    boolean randomizeQuestions,

    @NotNull
    @Valid
    List<TestQuestionUpdate> questions
) {
}
