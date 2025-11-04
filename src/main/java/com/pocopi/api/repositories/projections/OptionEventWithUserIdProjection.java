package com.pocopi.api.repositories.projections;

public interface OptionEventWithUserIdProjection {
    int getUserId();

    int getQuestionId();

    String getType();

    int getOptionId();

    long getTimestamp();
}
