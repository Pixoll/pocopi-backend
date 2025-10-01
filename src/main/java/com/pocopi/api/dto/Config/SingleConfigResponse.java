package com.pocopi.api.dto.Config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pocopi.api.dto.Form.Form;
import com.pocopi.api.dto.HomeFaq.Faq;
import com.pocopi.api.dto.HomeInfoCard.InformationCard;
import com.pocopi.api.dto.Image.SingleImageResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SingleConfigResponse(
    Optional<SingleImageResponse> icon,
    String title,
    String subtitle,
    String description,
    boolean anonymous,
    @JsonProperty("information_cards") List<InformationCard> informationCards,
    @JsonProperty("informed_consent") String informedConsent,
    List<Faq> faq,
    @JsonProperty("pre_test") Form preTest,
    @JsonProperty("post_test") Form postTest,
    Map<String, String> translations
    ) {
}
