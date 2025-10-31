package com.pocopi.api.migration.entities;

import com.pocopi.api.migration.entities.form.OldConfigForm;
import com.pocopi.api.migration.entities.home.OldConfigHomeFaq;
import com.pocopi.api.migration.entities.home.OldConfigHomeInfoCard;
import com.pocopi.api.migration.entities.test.OldConfigTestGroup;

import java.util.ArrayList;

public record OldConfig(
    OldConfigImage icon,
    String title,
    String subtitle,
    String description,
    boolean anonymous,
    ArrayList<OldConfigHomeInfoCard> informationCards,
    String informedConsent,
    ArrayList<OldConfigHomeFaq> frequentlyAskedQuestions,
    OldConfigForm preTestForm,
    OldConfigForm postTestForm,
    ArrayList<OldConfigTestGroup> groups,
    ArrayList<OldConfigTranslation> translations
) {
    public OldConfig {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Config title cannot be null or empty");
        }

        if (subtitle != null && subtitle.trim().isEmpty()) {
            subtitle = null;
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Config description cannot be null or empty");
        }

        if (informationCards == null) {
            informationCards = new ArrayList<>();
        }

        if (informedConsent == null || informedConsent.trim().isEmpty()) {
            throw new IllegalArgumentException("Config informed consent cannot be null or empty");
        }

        if (frequentlyAskedQuestions == null) {
            frequentlyAskedQuestions = new ArrayList<>();
        }

        if (groups == null) {
            groups = new ArrayList<>();
        }

        if (translations == null) {
            translations = new ArrayList<>();
        }

        if (!groups.isEmpty()) {
            final int probabilitySum = groups.stream()
                .reduce(0, (subtotal, group) -> subtotal + group.probability(), Integer::sum);

            if (probabilitySum != 100) {
                throw new IllegalArgumentException("Config groups probability sum must be 100, got " + probabilitySum);
            }
        }
    }
}
