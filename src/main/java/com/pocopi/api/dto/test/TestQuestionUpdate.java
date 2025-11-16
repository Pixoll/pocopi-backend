package com.pocopi.api.dto.test;

import com.pocopi.api.models.test.TestQuestionModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TestQuestionUpdate(
    @Min(1)
    Integer id,

    @Size(min = TestQuestionModel.TEXT_MIN_LEN, max = TestQuestionModel.TEXT_MAX_LEN)
    String text,

    @NotNull
    boolean randomizeOptions,

    @NotNull
    @Valid
    List<TestOptionUpdate> options
) {
}
