package com.pocopi.api.dto.form_question;

import com.pocopi.api.dto.form_question_option.FormOptionUpdate;
import com.pocopi.api.models.form.FormQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

public sealed interface FormQuestionUpdate
    permits FormQuestionUpdate.SelectMultipleUpdate,
    FormQuestionUpdate.SelectOneUpdate,
    FormQuestionUpdate.SliderUpdate,
    FormQuestionUpdate.TextLongUpdate,
    FormQuestionUpdate.TextShortUpdate {

        @AllArgsConstructor
        final class SelectMultipleUpdate implements FormQuestionUpdate {
            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<Integer> id;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String category;

            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<String> text;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public FormQuestionType type;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public List<FormOptionUpdate> options;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int min;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int max;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public boolean other;
        }

        @AllArgsConstructor
        final class SelectOneUpdate implements FormQuestionUpdate {
            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<Integer> id;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String category;

            @Schema(requiredMode = Schema.RequiredMode. NOT_REQUIRED)
            public Optional<String> text;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public FormQuestionType type;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public List<FormOptionUpdate> options;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public boolean other;
        }

        @AllArgsConstructor
        final class SliderUpdate implements FormQuestionUpdate {
            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<Integer> id;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String category;

            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<String> text;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public FormQuestionType type;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String placeholder;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int min;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int max;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int step;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public List<SliderLabel> labels;
        }

        @AllArgsConstructor
        final class TextLongUpdate implements FormQuestionUpdate {
            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<Integer> id;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String category;

            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<String> text;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public FormQuestionType type;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String placeholder;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int minLength;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int maxLength;
        }

        @AllArgsConstructor
        final class TextShortUpdate implements FormQuestionUpdate {
            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<Integer> id;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String category;

            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<String> text;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public FormQuestionType type;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String placeholder;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int minLength;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int maxLength;
        }
    }