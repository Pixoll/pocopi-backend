package com.pocopi.api.dto.test;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TestQuestionUpdate(
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Integer id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String text,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean randomizeOptions,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<TestOptionUpdate> options
) {
    public TestQuestionUpdate {
        if (text != null && text.isEmpty()) {
            text = null;
        }
    }
}
