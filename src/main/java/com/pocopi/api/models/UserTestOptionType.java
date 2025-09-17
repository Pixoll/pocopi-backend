package com.pocopi.api.models;

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
}
