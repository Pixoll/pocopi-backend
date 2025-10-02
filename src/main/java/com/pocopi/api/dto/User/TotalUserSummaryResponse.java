package com.pocopi.api.dto.User;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TotalUserSummaryResponse(
    @JsonProperty("average_accuracy") double averageAccuracy,
    @JsonProperty("average_time_taken") double averageTimeTaken,
    @JsonProperty("total_questions_answered") int totalQuestionsAnswered,
    List<UserSummaryResponse> users
) { }
