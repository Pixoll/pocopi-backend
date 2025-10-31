package com.pocopi.api.migration.entities.test;

import java.util.ArrayList;

public record OldConfigTestPhase(
    ArrayList<OldConfigTestQuestion> questions,
    boolean randomizeQuestions
) {
    public OldConfigTestPhase {
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("Test phase questions list cannot be null or empty");
        }
    }
}
