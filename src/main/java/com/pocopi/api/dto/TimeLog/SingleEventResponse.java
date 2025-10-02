package com.pocopi.api.dto.TimeLog;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SingleEventResponse(
    String type,
    @JsonProperty("option_id") int optionId,
    long timestamp
) {
}
