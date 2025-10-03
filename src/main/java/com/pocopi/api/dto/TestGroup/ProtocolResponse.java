package com.pocopi.api.dto.TestGroup;

import java.util.List;

public record ProtocolResponse(
    List<PhaseResponse> phases
) {
}
