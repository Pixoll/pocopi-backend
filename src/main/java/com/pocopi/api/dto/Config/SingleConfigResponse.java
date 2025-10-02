package com.pocopi.api.dto.Config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pocopi.api.dto.Form.Form;
import com.pocopi.api.dto.HomeFaq.Faq;
import com.pocopi.api.dto.HomeInfoCard.InformationCard;
import com.pocopi.api.dto.Image.SingleImageResponse;
import com.pocopi.api.dto.TestGroup.GroupResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SingleConfigResponse(
    Optional<SingleImageResponse> icon,
    String title,
    Optional<String> subtitle,
    String description,
    boolean anonymous,
    @JsonProperty("information_cards") List<InformationCard> informationCards,
    @JsonProperty("informed_consent") String informedConsent,
    List<Faq> faq,
    @JsonProperty("pre_test_form") Optional<Form> preTestForm,
    @JsonProperty("post_test_form") Optional<Form> postTestForm,
    List<GroupResponse> groups,
    Map<String, String> translations
) {
}
