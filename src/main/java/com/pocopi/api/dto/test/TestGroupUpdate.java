package com.pocopi.api.dto.test;

import com.pocopi.api.models.test.TestGroupModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TestGroupUpdate(
    @Min(1)
    Integer id,

    @NotNull
    @Min(TestGroupModel.PROBABILITY_MIN)
    @Max(TestGroupModel.PROBABILITY_MAX)
    int probability,

    @NotNull
    @Size(min = TestGroupModel.LABEL_MIN_LEN, max = TestGroupModel.LABEL_MAX_LEN)
    String label,

    @Size(min = TestGroupModel.GREETING_MIN_LEN, max = TestGroupModel.GREETING_MAX_LEN)
    String greeting,

    @NotNull
    boolean allowPreviousPhase,

    @NotNull
    boolean allowPreviousQuestion,

    @NotNull
    boolean allowSkipQuestion,

    @NotNull
    boolean randomizePhases,

    @NotNull
    @Size(max = 100)
    @Valid
    List<TestPhaseUpdate> phases
) {
}
