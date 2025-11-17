package com.pocopi.api.dto.config;

import com.pocopi.api.dto.form.Form;
import com.pocopi.api.dto.test.TestGroup;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record FullConfig(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int version,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
    Image icon,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
    String subtitle,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean anonymous,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Pattern usernamePattern,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<InformationCard> informationCards,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String informedConsent,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<FrequentlyAskedQuestion> frequentlyAskedQuestion,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
    Form preTestForm,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
    Form postTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<TestGroup> groups,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<Translation> translations
) {
}
