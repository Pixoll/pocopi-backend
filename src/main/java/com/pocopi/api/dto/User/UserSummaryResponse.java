package com.pocopi.api.dto.User;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserSummaryResponse(
    int id,
    String name,
    String email,
    int age,
    String group,
    long timestamp,
    @JsonProperty("time_taken") int timeTaken,
    @JsonProperty("correct_questions") int correctQuestions,
    @JsonProperty("questions_answered")int questionsAnswered,
    double accuracy
    ) {
}
