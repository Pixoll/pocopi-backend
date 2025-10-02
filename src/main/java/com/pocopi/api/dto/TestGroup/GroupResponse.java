package com.pocopi.api.dto.TestGroup;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroupResponse(
    float probability,
    String label,
    String greeting,
    @JsonProperty("protocol_response")ProtocolResponse protocolResponse
) {
}
