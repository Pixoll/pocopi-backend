package com.pocopi.api.models;

import lombok.Getter;

@Getter
public enum FormType {
    PRE("pre"),
    POST("post");

    private final String name;

    FormType(String name) {
        this.name = name;
    }
}
