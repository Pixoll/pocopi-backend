package com.pocopi.api.dto.config;

import com.pocopi.api.dto.form.FormUpdate;
import com.pocopi.api.dto.home_faq.FrequentlyAskedQuestionUpdate;
import com.pocopi.api.dto.home_info_card.InformationCardUpdate;
import com.pocopi.api.dto.test.TestGroupUpdate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ConfigUpdate(
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
    List<InformationCardUpdate> informationCards,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<FrequentlyAskedQuestionUpdate> faq,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    FormUpdate preTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    FormUpdate postTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, TestGroupUpdate> groups,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> translations
) {
}
