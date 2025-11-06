package com.pocopi.api.dto.form;

import com.pocopi.api.models.form.FormQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.util.List;

public sealed interface FormQuestionUpdate
    permits FormQuestionUpdate.SelectMultipleUpdate,
    FormQuestionUpdate.SelectOneUpdate,
    FormQuestionUpdate.SliderUpdate,
    FormQuestionUpdate.TextLongUpdate,
    FormQuestionUpdate.TextShortUpdate {
    @AllArgsConstructor
    final class SelectMultipleUpdate implements FormQuestionUpdate {
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public Integer id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {FormQuestionType.SELECT_MULTIPLE_NAME})
        public FormQuestionType type;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int min;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int max;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public boolean other;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public List<FormOptionUpdate> options;
    }

    @AllArgsConstructor
    final class SelectOneUpdate implements FormQuestionUpdate {
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public Integer id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {FormQuestionType.SELECT_ONE_NAME})
        public FormQuestionType type;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public boolean other;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public List<FormOptionUpdate> options;
    }

    @AllArgsConstructor
    final class SliderUpdate implements FormQuestionUpdate {
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public Integer id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {FormQuestionType.SLIDER_NAME})
        public FormQuestionType type;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int min;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int max;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int step;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public List<SliderLabelUpdate> labels;
    }

    @AllArgsConstructor
    final class TextLongUpdate implements FormQuestionUpdate {
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public Integer id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {FormQuestionType.TEXT_LONG_NAME})
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
        public Integer id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {FormQuestionType.TEXT_SHORT_NAME})
        public FormQuestionType type;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String placeholder;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int minLength;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int maxLength;
    }
}
