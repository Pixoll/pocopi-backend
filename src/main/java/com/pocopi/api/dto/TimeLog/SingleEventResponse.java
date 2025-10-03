package com.pocopi.api.dto.TimeLog;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SingleEventResponse(
    String type,
    int optionId,
    long timestamp
) {
}
