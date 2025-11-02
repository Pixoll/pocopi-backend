package com.pocopi.api.repositories.projections;

public interface TranslationWithDetailsProjection {
    String getKey();

    String getValue();

    String getDescription();

    String getArgumentsJson();
}
