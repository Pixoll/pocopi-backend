package com.pocopi.api.dto.HomeInfoCard;

import com.pocopi.api.dto.Image.SingleImageResponse;

import java.util.Optional;

public record InformationCard(
    String title,
    String description,
    String color,
    Optional<SingleImageResponse> icon
) {
}
