package com.pocopi.api.repositories.projections;

public interface UserFormAnswerProjection {
    int getId();

    int getConfigVersion();

    int getFormId();

    String getFormType();

    Integer getQuestionId();

    Integer getOptionId();

    Integer getValue();

    String getAnswer();
}
