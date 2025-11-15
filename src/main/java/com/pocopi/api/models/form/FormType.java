package com.pocopi.api.models.form;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum FormType {
    PRE("pre"),
    POST("post");

    private final String name;

    FormType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getValue() {
        return this.name;
    }

    public static FormType fromValue(String name) {
        return Stream.of(FormType.values())
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public String toString() {
        return this.name;
    }
}
