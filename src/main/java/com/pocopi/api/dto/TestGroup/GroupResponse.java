package com.pocopi.api.dto.TestGroup;

public record GroupResponse(
    int probability,
    String label,
    String greeting,
    ProtocolResponse protocolResponse
) {
}
