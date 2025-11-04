package com.pocopi.api.models.test;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum TestOptionEventType {
    DESELECT("deselect"),
    SELECT("select"),
    HOVER("hover");

    private final String name;

    TestOptionEventType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getValue() {
        return this.name;
    }

    public static TestOptionEventType fromValue(String name) {
        return Stream.of(TestOptionEventType.values())
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
