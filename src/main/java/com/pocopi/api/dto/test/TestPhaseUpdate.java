package com.pocopi.api.dto.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TestPhaseUpdate(
    @Min(1)
    Integer id,

    @NotNull
    boolean randomizeQuestions,

    @NotNull
    @Size(max = 100)
    @Valid
    List<TestQuestionUpdate> questions
) {
}
