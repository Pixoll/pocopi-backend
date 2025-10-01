package com.pocopi.api.repositories;

public interface FormProjection {
    Integer getQuestionId();
    String getQuestionType();
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
}
