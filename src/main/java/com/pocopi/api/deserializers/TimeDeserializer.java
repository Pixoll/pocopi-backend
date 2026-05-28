package com.pocopi.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.pocopi.api.exception.HttpException;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class TimeDeserializer extends StdDeserializer<LocalTime> {
    private static final String ERROR_MESSAGE = "Invalid time format. Expected mm:ss or hh:mm:ss.";

    public TimeDeserializer() {
        super(LocalTime.class);
    }

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        final String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(normalize(value.strip()));
        } catch (DateTimeParseException e) {
            throw HttpException.unprocessableEntity(ERROR_MESSAGE);
        }
    }

    private String normalize(String value) {
        final String[] parts = value.split(":");
        return switch (parts.length) {
            case 2 -> String.format("00:%02d:%02d",
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]));
            case 3 -> String.format("%02d:%02d:%02d",
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]));
            default -> throw HttpException.unprocessableEntity(ERROR_MESSAGE);
        };
    }
}
