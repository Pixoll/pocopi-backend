package com.pocopi.api.dto.test;

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
    @Min(0)
    @Max(100)
    int probability,

    @NotNull
    @Size(min = 1, max = 25)
    String label,

    @Size(min = 1, max = 2000)
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
    @Valid
    List<TestPhaseUpdate> phases
) {
}
