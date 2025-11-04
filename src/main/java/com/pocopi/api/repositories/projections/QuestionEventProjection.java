package com.pocopi.api.repositories.projections;

public interface QuestionEventProjection {
    int getQuestionId();

    String getTimestampsJson();

    int getCorrect();

    int getSkipped();

    Long getTotalOptionChanges();

    Long getTotalOptionHovers();
}
