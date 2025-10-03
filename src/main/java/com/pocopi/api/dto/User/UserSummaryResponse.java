package com.pocopi.api.dto.User;

public record UserSummaryResponse(
    int id,
    String name,
    String email,
    int age,
    String group,
    long timestamp,
    int timeTaken,
    int correctQuestions,
    int questionsAnswered,
    double accuracy
    ) {
}
