package com.pocopi.api.dto.config;

import com.pocopi.api.dto.form.FormUpdate;
import com.pocopi.api.dto.test.TestGroupUpdate;
import com.pocopi.api.models.config.ConfigModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ConfigUpdate(
    @NotNull
    @Size(min = ConfigModel.TITLE_MIN_LEN, max = ConfigModel.TITLE_MAX_LEN)
    String title,

    @NotNull
    @Size(min = ConfigModel.SUBTITLE_MIN_LEN, max = ConfigModel.SUBTITLE_MAX_LEN)
    String subtitle,

    @NotNull
    @Size(min = ConfigModel.DESCRIPTION_MIN_LEN, max = ConfigModel.DESCRIPTION_MAX_LEN)
    String description,

    @NotNull
    boolean anonymous,

    @Valid
    PatternUpdate usernamePattern,

    @NotNull
    @Size(min = ConfigModel.INFORMED_CONSENT_MIN_LEN, max = ConfigModel.INFORMED_CONSENT_MAX_LEN)
    String informedConsent,

    @Valid
    List<InformationCardUpdate> informationCards,

    @Valid
    List<FrequentlyAskedQuestionUpdate> faq,

    @Valid
    FormUpdate preTestForm,

    @Valid
    FormUpdate postTestForm,

    @NotNull
    @Valid
    List<TestGroupUpdate> groups,

    @NotNull
    @Valid
    List<TranslationUpdate> translations
) {
}
