package com.pocopi.api.dto.csv;

import java.util.List;
import java.util.stream.Stream;

public record UserTestResultCsvRow(
    String userId,
    String userUsername,
    String userAnonymous,
    String userName,
    String userEmail,
    String userAge,
    String configVersion,
    String attemptId,
    String group,
    String timestamp,
    String timeTaken,
    String correctQuestions,
    String questionsAnswered,
    String accuracy,
    String questionId,
    String questionStart,
    String questionEnd,
    String questionCorrect,
    String questionSkipped,
    String totalOptionChanges,
    String totalOptionHovers,
    String eventOptionId,
    String eventType,
    String eventTimestamp
) {
    public String toString(String delimiter) {
        final List<String> values = Stream.of(
            userId,
            userUsername,
            userAnonymous,
            userName,
            userEmail,
            userAge,
            configVersion,
            attemptId,
            group,
            timestamp,
            timeTaken,
            correctQuestions,
            questionsAnswered,
            accuracy,
            questionId,
            questionStart,
            questionEnd,
            questionCorrect,
            questionSkipped,
            totalOptionChanges,
            totalOptionHovers,
            eventOptionId,
            eventType,
            eventTimestamp
        ).map(v -> v != null ? v : "").toList();

        return String.join(delimiter, values);
    }
}
