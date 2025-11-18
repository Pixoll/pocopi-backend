package com.pocopi.api.repositories.projections;

public interface LastSelectedOptionProjection {
    int getQuestionId();

    boolean getCorrect();
}
