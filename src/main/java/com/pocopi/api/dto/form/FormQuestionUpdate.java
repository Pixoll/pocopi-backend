package com.pocopi.api.dto.form;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pocopi.api.models.form.FormQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(
            value = FormQuestionUpdate.SelectMultipleUpdate.class,
            name = FormQuestionType.SELECT_MULTIPLE_NAME
        ),
        @JsonSubTypes.Type(value = FormQuestionUpdate.SelectOneUpdate.class, name = FormQuestionType.SELECT_ONE_NAME),
        @JsonSubTypes.Type(value = FormQuestionUpdate.SliderUpdate.class, name = FormQuestionType.SLIDER_NAME),
        @JsonSubTypes.Type(value = FormQuestionUpdate.TextLongUpdate.class, name = FormQuestionType.TEXT_LONG_NAME),
        @JsonSubTypes.Type(value = FormQuestionUpdate.TextShortUpdate.class, name = FormQuestionType.TEXT_SHORT_NAME)
    }
)
public sealed interface FormQuestionUpdate
    permits FormQuestionUpdate.SelectMultipleUpdate,
    FormQuestionUpdate.SelectOneUpdate,
    FormQuestionUpdate.SliderUpdate,
    FormQuestionUpdate.TextLongUpdate,
    FormQuestionUpdate.TextShortUpdate {
    @AllArgsConstructor
    @NoArgsConstructor
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
    @NoArgsConstructor
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
    @NoArgsConstructor
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
    @NoArgsConstructor
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
    @NoArgsConstructor
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
