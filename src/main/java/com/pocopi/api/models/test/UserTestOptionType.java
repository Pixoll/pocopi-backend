package com.pocopi.api.models.test;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserTestOptionType {
    DESELECT("deselect"),
    SELECT("select"),
    HOVER("hover");

    private final String name;

    UserTestOptionType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getValue() {
        return this.name;
    }
}
