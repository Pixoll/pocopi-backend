package com.pocopi.api.dto.User;

import java.util.List;

public record TotalUserSummaryResponse(
    double averageAccuracy,
    double averageTimeTaken,
    int totalQuestionsAnswered,
    List<UserSummaryResponse> users
) { }
