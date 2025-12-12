package com.pocopi.api.dto.csv;

public enum ResultCsvType {
    FORMS("forms"),
    TEST("test");

    private final String name;

    ResultCsvType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
