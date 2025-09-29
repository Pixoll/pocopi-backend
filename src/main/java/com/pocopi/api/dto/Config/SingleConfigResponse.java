package com.pocopi.api.dto.Config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SingleConfigResponse(
        int version,
        @JsonProperty("icon_id") int iconId,
        String title,
        String subtitle,
        String description,
        @JsonProperty("informed_consent") String informedConsent
) {
}
