package com.pocopi.api.repositories.projections;

public interface UserTestAttemptWithGroupProjection {
    long getId();

    String getGroup();

    long getStart();

    long getEnd();
}
