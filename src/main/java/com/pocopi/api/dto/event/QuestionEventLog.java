package com.pocopi.api.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record QuestionEventLog(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int questionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<QuestionTimestamp> timestamps,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean correct,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean skipped,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalOptionChanges,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int totalOptionHovers,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<OptionSelectionEvent> optionSelections,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<OptionEventLog> events
) {
}
