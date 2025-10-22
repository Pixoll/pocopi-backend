package com.pocopi.api.dto.config;

import com.pocopi.api.dto.form.Form;
import com.pocopi.api.dto.home_faq.FrequentlyAskedQuestion;
import com.pocopi.api.dto.home_info_card.InformationCard;
import com.pocopi.api.dto.image.Image;
import com.pocopi.api.dto.test.TestGroup;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Config(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Configuration version")
    int id,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Configuration icon image")
    Optional<Image> icon,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Configuration title")
    String title,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Configuration subtitle")
    Optional<String> subtitle,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Configuration description")
    String description,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Is or not anonymous")
    boolean anonymous,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Card information by configuration")
    List<InformationCard> informationCards,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String informedConsent,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<FrequentlyAskedQuestion> frequentlyAskedQuestion,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Optional<Form> preTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Optional<Form> postTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, TestGroup> groups,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> translations
) {
}
