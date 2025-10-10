package com.pocopi.api.dto.FormQuestion;

import com.pocopi.api.dto.FormQuestionOption.FormOption;
import com.pocopi.api.dto.FormQuestionOption.PatchFormOption;
import com.pocopi.api.dto.Image.Image;
import com.pocopi.api.dto.SliderLabel.SliderLabel;
import com.pocopi.api.models.FormQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

public sealed interface PatchFormQuestion
    permits PatchFormQuestion.PatchSelectMultiple,
    PatchFormQuestion.PatchSelectOne,
    PatchFormQuestion.PatchSlider,
    PatchFormQuestion.PatchTextLong,
    PatchFormQuestion.PatchTextShort {

        @AllArgsConstructor
        final class PatchSelectMultiple implements PatchFormQuestion {
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int id;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String category;

            @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            public Optional<String> text;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public FormQuestionType type;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public List<PatchFormOption> options;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int min;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int max;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public boolean other;
        }

        @AllArgsConstructor
        final class PatchSelectOne implements PatchFormQuestion {
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int id;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public String category;

            @Schema(requiredMode = Schema.RequiredMode. NOT_REQUIRED)
            public Optional<String> text;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public FormQuestionType type;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public List<PatchFormOption> options;

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public boolean other;
        }

        @AllArgsConstructor
        final class PatchSlider implements PatchFormQuestion {
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int id;

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
        final class PatchTextLong implements PatchFormQuestion {
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int id;

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
        final class PatchTextShort implements PatchFormQuestion {
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            public int id;

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