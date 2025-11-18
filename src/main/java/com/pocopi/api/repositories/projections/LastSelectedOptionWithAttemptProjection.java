package com.pocopi.api.repositories.projections;

public interface LastSelectedOptionWithAttemptProjection {
    long getAttemptId();

    int getUserId();

    int getQuestionId();

    boolean getCorrect();
}
