package com.pocopi.api.repositories.projections;

import com.pocopi.api.models.form.FormQuestionType;

import java.util.stream.Stream;

public interface FormProjection {
    Integer getFormId();

    Integer getConfigVersion();

    Integer getQuestionId();

    String getQuestionTypeString();

    String getCategory();

    String getQuestionText();

    String getPlaceholder();

    Integer getMin();

    Integer getMax();

    Integer getStep();

    Integer getMinLength();

    Integer getMaxLength();

    Boolean getOther();

    String getQuestionImagePath();

    Integer getOptionId();

    String getOptionText();

    String getOptionImagePath();

    Integer getSliderValue();

    String getSliderLabel();

    default FormQuestionType getQuestionType() {
        final String name = getQuestionTypeString();

        if (name == null) {
            return null;
        }

        return Stream.of(FormQuestionType.values())
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}

//@AllArgsConstructor
//@Getter
//public class FormProjection {
//    Integer formId;
//    Integer configVersion;
//    Integer questionId;
//    @Convert(converter = FormQuestionTypeConverter.class)
//    FormQuestionType questionType;
//    String category;
//    String questionText;
//    String placeholder;
//    Integer min;
//    Integer max;
//    Integer step;
//    Integer minLength;
//    Integer maxLength;
//    Boolean other;
//    String questionImagePath;
//    Integer optionId;
//    String optionText;
//    String optionImagePath;
//    Integer sliderValue;
//    String sliderLabel;
//}
