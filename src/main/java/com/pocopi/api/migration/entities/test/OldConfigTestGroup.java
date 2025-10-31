package com.pocopi.api.migration.entities.test;

public record OldConfigTestGroup(
    String label,
    byte probability,
    String greeting,
    OldConfigTestProtocol protocol
) {
    public OldConfigTestGroup {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Test group label cannot be null or empty");
        }

        if (probability <= 0 || probability > 100) {
            throw new IllegalArgumentException("Test group probability must be between 1 and 100");
        }

        if (greeting != null && greeting.trim().isEmpty()) {
            greeting = null;
        }
    }
}
