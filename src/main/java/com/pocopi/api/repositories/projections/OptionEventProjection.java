package com.pocopi.api.repositories.projections;

public interface OptionEventProjection {
    int getConfigVersion();

    long getAttemptId();

    int getQuestionId();

    String getType();

    int getOptionId();

    long getTimestamp();
}
