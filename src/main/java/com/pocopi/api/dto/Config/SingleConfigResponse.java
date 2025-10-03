package com.pocopi.api.dto.Config;

import com.pocopi.api.dto.Form.Form;
import com.pocopi.api.dto.HomeFaq.Faq;
import com.pocopi.api.dto.HomeInfoCard.InformationCard;
import com.pocopi.api.dto.Image.Image;
import com.pocopi.api.dto.TestGroup.Group;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SingleConfigResponse(
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
    List<Faq> faq,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Optional<Form> preTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Optional<Form> postTestForm,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, Group> groups,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, String> translations
) {
}
