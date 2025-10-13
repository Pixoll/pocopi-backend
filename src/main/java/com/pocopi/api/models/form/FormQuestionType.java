package com.pocopi.api.models.form;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum FormQuestionType {
    SELECT_ONE("select-one"),
    SELECT_MULTIPLE("select-multiple"),
    SLIDER("slider"),
    TEXT_SHORT("text-short"),
    TEXT_LONG("text-long");

    private final String name;

    FormQuestionType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getValue() {
        return this.name;
    }
}
