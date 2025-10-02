package com.pocopi.api.models;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

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
}
