package com.pocopi.api.dto.TimeLog;

import java.util.List;

public record SingleTimeLogResponse(
    int userId,
    int phaseId,
    int questionId,
    long startTimestamp,
    long endTimestamp,
    boolean skipped,
    boolean correct,
    int totalOptionChanges,
    int totalOptionHovers,
    List<SingleEventResponse> events
) {
}
