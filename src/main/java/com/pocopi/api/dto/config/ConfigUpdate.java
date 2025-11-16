package com.pocopi.api.dto.config;

import com.pocopi.api.dto.form.FormUpdate;
import com.pocopi.api.dto.test.TestGroupUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record ConfigUpdate(
    @NotNull
    @Size(min = 1, max = 100)
    String title,

    @NotNull
    @Size(min = 1, max = 200)
    String subtitle,

    @NotNull
    @Size(min = 1, max = 2000)
    String description,

    @NotNull
    boolean anonymous,

    @NotNull
    @Size(min = 1, max = 2000)
    String informedConsent,

    @Valid
    List<InformationCardUpdate> informationCards,

    @Valid
    List<FrequentlyAskedQuestionUpdate> faq,

    @Valid
    FormUpdate preTestForm,

    @Valid
    FormUpdate postTestForm,

    @Valid
    @NotNull
    List<TestGroupUpdate> groups,

    // TODO add checks to keys and values
    @NotNull
    Map<String, String> translations
) {
}
