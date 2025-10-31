package com.pocopi.api.migration.entities.test;

import java.util.ArrayList;

public record OldConfigTestProtocol(
    String label,
    ArrayList<OldConfigTestPhase> phases,
    boolean randomizePhases,
    boolean allowPreviousPhase,
    boolean allowPreviousQuestion,
    boolean allowSkipQuestion
) {
    public OldConfigTestProtocol {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Test protocol label cannot be null or empty");
        }

        if (phases == null || phases.isEmpty()) {
            throw new IllegalArgumentException("Test protocol phases list cannot be null or empty");
        }
    }
}
