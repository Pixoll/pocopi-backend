package com.pocopi.api.dto.HomeInfoCard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pocopi.api.dto.Image.SingleImageResponse;

import java.util.Optional;

public record InformationCard(
        String title,
        String description,
        int color,
        Optional<SingleImageResponse> icon
) {
}
