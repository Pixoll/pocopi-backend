package com.pocopi.api.repositories.projections;

public interface UserTestAttemptWithGroupProjection {
    long getId();

    int getConfigVersion();

    String getGroup();

    long getStart();

    long getEnd();
}
