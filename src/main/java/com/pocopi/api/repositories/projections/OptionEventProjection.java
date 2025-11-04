package com.pocopi.api.repositories.projections;

public interface OptionEventProjection {
    int getQuestionId();

    String getType();

    int getOptionId();

    long getTimestamp();
}
