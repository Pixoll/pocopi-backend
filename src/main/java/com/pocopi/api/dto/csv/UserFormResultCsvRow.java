package com.pocopi.api.dto.csv;

import java.util.List;
import java.util.stream.Stream;

public record UserFormResultCsvRow(
    String userId,
    String userUsername,
    String userAnonymous,
    String userName,
    String userEmail,
    String userAge,
    String configVersion,
    String attemptId,
    String formType,
    String timestamp,
    String questionId,
    String optionId,
    String value,
    String answer
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
            formType,
            timestamp,
            questionId,
            optionId,
            value,
            answer
        ).map(v -> v != null ? v : "").toList();

        return String.join(delimiter, values);
    }
}
