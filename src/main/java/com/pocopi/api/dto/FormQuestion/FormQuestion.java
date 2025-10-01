package com.pocopi.api.dto.FormQuestion;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pocopi.api.dto.FormQuestionOption.FormOption;
import com.pocopi.api.dto.SliderLabel.SliderLabel;

import java.util.List;

public sealed interface FormQuestion permits
    FormQuestion.SelectMultiple,
    FormQuestion.SelectOne,
    FormQuestion.Slider,
    FormQuestion.TextLong,
    FormQuestion.TextShort {

    record SelectMultiple(
        @JsonProperty("base_question") BaseQuestion baseQuestion,
        String type,
        List<FormOption> options,
        int min,
        int max,
        boolean other
    ) implements FormQuestion {
    }

    record SelectOne(
        @JsonProperty("base_question") BaseQuestion baseQuestion,
        String type,
        List<FormOption> options,
        boolean other
    ) implements FormQuestion {
    }

    record Slider(
        @JsonProperty("base_question") BaseQuestion baseQuestion,
        String type,
        String placeholder,
        int min,
        int max,
        int step,
        List<SliderLabel> labels
    ) implements FormQuestion {
    }

    record TextLong(
        @JsonProperty("base_question") BaseQuestion baseQuestion,
        String type,
        String placeholder,
        @JsonProperty("min_length") int minLength,
        @JsonProperty("max_length") int maxLength
    ) implements FormQuestion {
    }

    record TextShort(
        @JsonProperty("base_question") BaseQuestion baseQuestion,
        String type,
        String placeholder,
        @JsonProperty("min_length") int minLength,
        @JsonProperty("max_length") int maxLength
    ) implements FormQuestion {
    }

}
