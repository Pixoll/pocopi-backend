package com.pocopi.api.dto.config;

import com.pocopi.api.dto.form.FormUpdate;
import com.pocopi.api.dto.test.TestGroupUpdate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record ConfigUpdate(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String subtitle,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean anonymous,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String informedConsent,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<InformationCardUpdate> informationCards,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    List<FrequentlyAskedQuestionUpdate> faq,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    FormUpdate preTestForm,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    FormUpdate postTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<TestGroupUpdate> groups,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> translations
) {
}
