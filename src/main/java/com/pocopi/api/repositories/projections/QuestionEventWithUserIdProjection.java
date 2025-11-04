package com.pocopi.api.repositories.projections;

public interface QuestionEventWithUserIdProjection {
    int getUserId();

    int getQuestionId();

    String getTimestampsJson();

    int getCorrect();

    int getSkipped();

    Long getTotalOptionChanges();

    Long getTotalOptionHovers();
}
