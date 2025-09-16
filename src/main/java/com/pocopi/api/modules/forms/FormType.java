package com.pocopi.api.modules.forms;

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