package com.pocopi.api.migration.entities.home;

public record OldConfigHomeFaq(
    String question,
    String answer
) {
    public OldConfigHomeFaq {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Home FAQ question cannot be null or empty");
        }

        if (answer == null || answer.trim().isEmpty()) {
            throw new IllegalArgumentException("Home FAQ answer cannot be null or empty");
        }
    }
}
