package com.pocopi.api.dto.Config;

import com.pocopi.api.dto.Form.PatchForm;
import com.pocopi.api.dto.HomeFaq.PatchFaq;
import com.pocopi.api.dto.HomeInfoCard.PatchInformationCard;
import com.pocopi.api.dto.TestGroup.PatchGroup;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record PatchLastConfig(

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int version,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Optional<String> subtitle,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean anonymous,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String informedConsent,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<PatchInformationCard> informationCards,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<PatchFaq> faq,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PatchForm preTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PatchForm postTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, PatchGroup> groups,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> translations
    ) {
}
