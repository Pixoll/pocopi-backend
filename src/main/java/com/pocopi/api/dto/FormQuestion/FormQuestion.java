package com.pocopi.api.dto.FormQuestion;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pocopi.api.dto.FormQuestionOption.FormOption;
import com.pocopi.api.dto.Image.SingleImageResponse;
import com.pocopi.api.dto.SliderLabel.SliderLabel;
import com.pocopi.api.models.FormQuestionType;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public sealed interface FormQuestion
    permits FormQuestion.SelectMultiple,
    FormQuestion.SelectOne,
    FormQuestion.Slider,
    FormQuestion.TextLong,
    FormQuestion.TextShort {

    @AllArgsConstructor
    final class SelectMultiple implements FormQuestion {
        public int id;
        public String category;
        public Optional<String> text;
        public Optional<SingleImageResponse> image;
        public FormQuestionType type;
        public List<FormOption> options;
        public int min;
        public int max;
        public boolean other;
    }

    @AllArgsConstructor
    final class SelectOne implements FormQuestion {
        public int id;
        public String category;
        public Optional<String> text;
        public Optional<SingleImageResponse> image;
        public FormQuestionType type;
        public List<FormOption> options;
        public boolean other;
    }

    @AllArgsConstructor
    final class Slider implements FormQuestion {
        public int id;
        public String category;
        public Optional<String> text;
        public Optional<SingleImageResponse> image;
        public FormQuestionType type;
        public String placeholder;
        public int min;
        public int max;
        public int step;
        public List<SliderLabel> labels;
    }

    @AllArgsConstructor
    final class TextLong implements FormQuestion {
        public int id;
        public String category;
        public Optional<String> text;
        public Optional<SingleImageResponse> image;
        public FormQuestionType type;
        public String placeholder;
        @JsonProperty("min_length")
        public int minLength;
        @JsonProperty("max_length")
        public int maxLength;
    }

    @AllArgsConstructor
    final class TextShort implements FormQuestion {
        public int id;
        public String category;
        public Optional<String> text;
        public Optional<SingleImageResponse> image;
        public FormQuestionType type;
        public String placeholder;
        public int minLength;
        public int maxLength;
    }
}
