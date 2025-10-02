package com.pocopi.api.dto.TimeLog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SingleTimeLogResponse(
    @JsonProperty("user_id") int userId,
    @JsonProperty("phase_id") int phaseId,
    @JsonProperty("question_id") int questionId,
    @JsonProperty("start_timestamp") long startTimestamp,
    @JsonProperty("end_timestamp") long endTimestamp,
    boolean skipped,
    boolean correct,
    @JsonProperty("total_option_changes") int totalOptionChanges,
    @JsonProperty("total_option_hovers") int totalOptionHovers,
    List<SingleEventResponse> events
) {
}
