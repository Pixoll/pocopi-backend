package com.pocopi.api.repositories.projections;

public interface LastSelectedOptionWithAttemptProjection {
    long getAttemptId();

    int getQuestionId();

    boolean getCorrect();
}
