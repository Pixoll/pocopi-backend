package com.pocopi.api.migration.entities.form;

import com.pocopi.api.migration.entities.OldConfigImage;
import com.pocopi.api.models.form.FormQuestionType;

import java.util.ArrayList;

public record OldConfigFormQuestion(
    String category,
    FormQuestionType type,
    String text,
    OldConfigImage image,
    boolean required,
    Short min,
    Short max,
    Short step,
    Boolean other,
    Short minLength,
    Short maxLength,
    String placeholder,
    ArrayList<OldConfigFormOption> options,
    ArrayList<OldConfigFormSliderLabel> labels
) {
    public OldConfigFormQuestion {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Form question category cannot be null or empty");
        }

        if (type == null) {
            throw new IllegalArgumentException("Form question type cannot be null");
        }

        if ((text == null || text.trim().isEmpty()) && image == null) {
            throw new IllegalArgumentException(
                "Form question cannot have both text and image be null or empty at the same time"
            );
        }

        if (text != null && text.trim().isEmpty()) {
            text = null;
        }

        switch (type) {
            case SELECT_ONE -> {
                if (options == null || options.isEmpty()) {
                    throw new IllegalArgumentException(
                        "Form question (select-one) options list cannot be null or empty"
                    );
                }

                if (other == null) {
                    other = false;
                }

                min = null;
                max = null;
                step = null;
                minLength = null;
                maxLength = null;
                placeholder = null;
                labels = new ArrayList<>();
            }

            case SELECT_MULTIPLE -> {
                if (options == null || options.isEmpty()) {
                    throw new IllegalArgumentException(
                        "Form question (select-multiple) options list cannot be null or empty"
                    );
                }

                if (min == null || min < 0) {
                    throw new IllegalArgumentException(
                        "Form question (select-multiple) min cannot be null or negative"
                    );
                }

                if (max == null || max < min) {
                    throw new IllegalArgumentException(
                        "Form question (select-multiple) max cannot be null or less than min"
                    );
                }

                if (other == null) {
                    other = false;
                }

                step = null;
                minLength = null;
                maxLength = null;
                placeholder = null;
                labels = new ArrayList<>();
            }

            case SLIDER -> {
                if (min == null || min < 0) {
                    throw new IllegalArgumentException("Form question (slider) min cannot be null or negative");
                }

                if (max == null || max < min) {
                    throw new IllegalArgumentException("Form question (slider) max cannot be null or less than min");
                }

                if (step == null || step <= 0) {
                    throw new IllegalArgumentException("Form question (slider) step cannot be null or non-positive");
                }

                if (labels == null) {
                    labels = new ArrayList<>();
                }

                for (final OldConfigFormSliderLabel label : labels) {
                    final short number = label.number();

                    if (number < min || number > max) {
                        throw new IllegalArgumentException("Form question (slider) label number out of bounds");
                    }

                    if (number % step != 0) {
                        throw new IllegalArgumentException(
                            "Form question (slider) label number is not multiple of step"
                        );
                    }
                }

                other = null;
                minLength = null;
                maxLength = null;
                placeholder = null;
                options = new ArrayList<>();
            }

            case TEXT_SHORT,
                 TEXT_LONG -> {
                if (placeholder == null || placeholder.trim().isEmpty()) {
                    throw new IllegalArgumentException("Form question (text) placeholder cannot be null or empty");
                }

                if (minLength == null || minLength < 1) {
                    throw new IllegalArgumentException(
                        "Form question (slider) min length cannot be null or less than 1"
                    );
                }

                if (maxLength == null || maxLength < minLength) {
                    throw new IllegalArgumentException(
                        "Form question (slider) max length cannot be null or less than min length"
                    );
                }

                min = null;
                max = null;
                step = null;
                other = null;
                options = new ArrayList<>();
                labels = new ArrayList<>();
            }
        }
    }
}
