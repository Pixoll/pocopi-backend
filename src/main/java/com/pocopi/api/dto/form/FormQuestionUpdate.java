package com.pocopi.api.dto.form;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    record SelectMultipleUpdate(
        @Min(1)
        Integer id,

        @NotNull
        @Size(min = FormQuestionModel.CATEGORY_MIN_LEN, max = FormQuestionModel.CATEGORY_MAX_LEN)
        String category,

        @Size(min = FormQuestionModel.TEXT_MIN_LEN, max = FormQuestionModel.TEXT_MAX_LEN)
        String text,

        @NotNull
        @Schema(allowableValues = {FormQuestionType.SELECT_MULTIPLE_NAME})
        FormQuestionType type,

        @NotNull
        @Min(0x0000)
        @Max(0xffff)
        int min,

        @NotNull
        @Min(0x0000)
        @Max(0xffff)
        int max,

        @NotNull
        boolean other,

        @NotNull
        @Size(max = 100)
        @Valid
        List<FormOptionUpdate> options
    ) implements FormQuestionUpdate {
    }

    record SelectOneUpdate(
        @Min(1)
        Integer id,

        @NotNull
        @Size(min = FormQuestionModel.CATEGORY_MIN_LEN, max = FormQuestionModel.CATEGORY_MAX_LEN)
        String category,

        @Size(min = FormQuestionModel.TEXT_MIN_LEN, max = FormQuestionModel.TEXT_MAX_LEN)
        String text,

        @NotNull
        @Schema(allowableValues = {FormQuestionType.SELECT_ONE_NAME})
        FormQuestionType type,

        @NotNull
        boolean other,

        @NotNull
        @Size(max = 100)
        @Valid
        List<FormOptionUpdate> options
    ) implements FormQuestionUpdate {
    }

    record SliderUpdate(
        @Min(1)
        Integer id,

        @NotNull
        @Size(min = FormQuestionModel.CATEGORY_MIN_LEN, max = FormQuestionModel.CATEGORY_MAX_LEN)
        String category,

        @Size(min = FormQuestionModel.TEXT_MIN_LEN, max = FormQuestionModel.TEXT_MAX_LEN)
        String text,

        @NotNull
        @Schema(allowableValues = {FormQuestionType.SLIDER_NAME})
        FormQuestionType type,

        @NotNull
        @Min(0x0000)
        @Max(0xffff)
        int min,

        @NotNull
        @Min(0x0000)
        @Max(0xffff)
        int max,

        @NotNull
        @Min(0x0000)
        @Max(0xffff)
        int step,

        @NotNull
        @Size(max = 100)
        @Valid
        List<SliderLabelUpdate> labels
    ) implements FormQuestionUpdate {
    }

    record TextLongUpdate(
        @Min(1)
        Integer id,

        @NotNull
        @Size(min = FormQuestionModel.CATEGORY_MIN_LEN, max = FormQuestionModel.CATEGORY_MAX_LEN)
        String category,

        @Size(min = FormQuestionModel.TEXT_MIN_LEN, max = FormQuestionModel.TEXT_MAX_LEN)
        String text,

        @NotNull
        @Schema(allowableValues = {FormQuestionType.TEXT_LONG_NAME})
        FormQuestionType type,

        @NotNull
        @Size(min = FormQuestionModel.PLACEHOLDER_MIN_LEN, max = FormQuestionModel.PLACEHOLDER_MAX_LEN)
        String placeholder,

        @NotNull
        @Min(0x0000)
        @Max(0xffff)
        int minLength,

        @NotNull
        @Min(0x0000)
        @Max(0xffff)
        int maxLength
    ) implements FormQuestionUpdate {
    }

    record TextShortUpdate(
        @Min(1)
        Integer id,

        @NotNull
        @Size(min = FormQuestionModel.CATEGORY_MIN_LEN, max = FormQuestionModel.CATEGORY_MAX_LEN)
        String category,

        @Size(min = FormQuestionModel.TEXT_MIN_LEN, max = FormQuestionModel.TEXT_MAX_LEN)
        String text,

        @NotNull
        @Schema(allowableValues = {FormQuestionType.TEXT_SHORT_NAME})
        FormQuestionType type,

        @NotNull
        @Size(min = FormQuestionModel.PLACEHOLDER_MIN_LEN, max = FormQuestionModel.PLACEHOLDER_MAX_LEN)
        String placeholder,

        @NotNull
        @Min(0x0000)
        @Max(0xffff)
        int minLength,

        @NotNull
        @Min(0x0000)
        @Max(0xffff)
        int maxLength
    ) implements FormQuestionUpdate {
    }
}
