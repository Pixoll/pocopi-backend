package com.pocopi.api.unit.config;

import com.pocopi.api.models.config.TranslationKeyModel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TranslationKeyModelTest {

    @Test
    void testBuilderAndFields() {
        String key = "greet";
        String value = "Hola, %s!";
        List<String> arguments = Arrays.asList("name");

        TranslationKeyModel model = TranslationKeyModel.builder()
                .key(key)
                .value(value)
                .arguments(arguments)
                .build();

        assertEquals(key, model.getKey());
        assertEquals(value, model.getValue());
        assertEquals(arguments, model.getArguments());
        assertEquals(0, model.getId());
    }

    @Test
    void testSetterAndGetter() {
        TranslationKeyModel model = new TranslationKeyModel();
        String key = "farewell";
        String value = "Adiós, %s!";
        List<String> args = Arrays.asList("name", "date");

        model.setKey(key);
        model.setValue(value);
        model.setArguments(args);

        assertEquals(key, model.getKey());
        assertEquals(value, model.getValue());
        assertEquals(args, model.getArguments());
    }
}
