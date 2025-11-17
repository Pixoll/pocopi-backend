package com.pocopi.api.dto.form;

import com.pocopi.api.dto.config.Image;
import com.pocopi.api.models.form.FormQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.util.List;

@SuppressWarnings("unused")
public sealed interface FormQuestion
    permits FormQuestion.SelectMultiple,
    FormQuestion.SelectOne,
    FormQuestion.Slider,
    FormQuestion.TextLong,
    FormQuestion.TextShort {

    @AllArgsConstructor
    final class SelectMultiple implements FormQuestion {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
        public Image image;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {FormQuestionType.SELECT_MULTIPLE_NAME})
        public FormQuestionType type;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public List<FormOption> options;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int min;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int max;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public boolean other;
    }

    @AllArgsConstructor
    final class SelectOne implements FormQuestion {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
        public Image image;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {FormQuestionType.SELECT_ONE_NAME})
        public FormQuestionType type;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public List<FormOption> options;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public boolean other;
    }

    @AllArgsConstructor
    final class Slider implements FormQuestion {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
        public Image image;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {FormQuestionType.SLIDER_NAME})
        public FormQuestionType type;

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
    final class TextLong implements FormQuestion {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
        public Image image;

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
    final class TextShort implements FormQuestion {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public int id;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String category;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"string", "null"})
        public String text;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, types = {"object", "null"})
        public Image image;

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
