package com.pocopi.api.dto.Config;

import com.pocopi.api.dto.Form.Form;
import com.pocopi.api.dto.HomeFaq.Faq;
import com.pocopi.api.dto.HomeInfoCard.InformationCard;
import com.pocopi.api.dto.Image.SingleImageResponse;
import com.pocopi.api.dto.TestGroup.GroupResponse;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SingleConfigResponse(
    Optional<SingleImageResponse> icon,
    String title,
    Optional<String> subtitle,
    String description,
    boolean anonymous,
    List<InformationCard> informationCards,
    String informedConsent,
    List<Faq> faq,
    Optional<Form> preTestForm,
    Optional<Form> postTestForm,
    @NotNull
    Map<String,GroupResponse> groups,
    Map<String, String> translations
) {
}
