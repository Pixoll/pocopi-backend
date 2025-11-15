package com.pocopi.api.models.form;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum FormQuestionType {
    SELECT_ONE("select-one"),
    SELECT_MULTIPLE("select-multiple"),
    SLIDER("slider"),
    TEXT_SHORT("text-short"),
    TEXT_LONG("text-long");

    public static final String SELECT_ONE_NAME = "select-one";
    public static final String SELECT_MULTIPLE_NAME = "select-multiple";
    public static final String SLIDER_NAME = "slider";
    public static final String TEXT_SHORT_NAME = "text-short";
    public static final String TEXT_LONG_NAME = "text-long";

    private final String name;

    FormQuestionType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getValue() {
        return this.name;
    }

    public static FormQuestionType fromValue(String name) {
        return Stream.of(FormQuestionType.values())
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public String toString() {
        return this.name;
    }
}
