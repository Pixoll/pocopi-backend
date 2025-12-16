package com.pocopi.api.repositories.projections;

public interface UserTestAttemptWithGroupProjection {
    long getId();

    int getConfigVersion();

    String getGroup();

    int getUserId();

    long getStart();

    long getEnd();
}
