package com.pocopi.api.modules.FormQuestion;

import lombok.Getter;

@Getter
public enum TextType {
    SELECT_ONE("select-one"),
    SELECT_MULTIPLE("select-multiple"),
    SLIDER("slider"),
    TEXT_SHORT("text-short"),
    TEXT_LONG("text-long");

    private final String name;

    TextType(String name) {
        this.name = name;
    }

}