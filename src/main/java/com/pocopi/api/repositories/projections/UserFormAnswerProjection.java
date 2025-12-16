package com.pocopi.api.repositories.projections;

public interface UserFormAnswerProjection {
    long getAttemptId();

    int getConfigVersion();

    String getFormType();

    long getTimestamp();

    Integer getQuestionId();

    Integer getOptionId();

    Integer getValue();

    String getAnswer();
}
