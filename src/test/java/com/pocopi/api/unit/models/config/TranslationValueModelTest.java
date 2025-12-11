package com.pocopi.api.unit.models.config;

import com.pocopi.api.models.config.TranslationValueModel;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.TranslationKeyModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TranslationValueModelTest {

    @Test
    void testBuilderAndFields() {
        ConfigModel config = ConfigModel.builder()
                .title("Config Title")
                .description("Some description")
                .informedConsent("Consent")
                .build();

        TranslationKeyModel key = TranslationKeyModel.builder()
                .key("welcome")
                .value("Welcome, %s!")
                .arguments(List.of("name"))
                .build();

        String textValue = "Welcome, John!";

        TranslationValueModel tvm = TranslationValueModel.builder()
                .config(config)
                .key(key)
                .value(textValue)
                .build();

        assertEquals(config, tvm.getConfig());
        assertEquals(key, tvm.getKey());
        assertEquals(textValue, tvm.getValue());
        assertEquals(0L, tvm.getId());
    }

    @Test
    void testSettersAndGetters() {
        TranslationValueModel tvm = new TranslationValueModel();
        ConfigModel config = ConfigModel.builder()
                .title("Config")
                .description("D")
                .informedConsent("C")
                .build();

        TranslationKeyModel key = TranslationKeyModel.builder()
                .key("goodbye")
                .value("Bye, %s!")
                .arguments(List.of("name"))
                .build();

        tvm.setConfig(config);
        tvm.setKey(key);
        tvm.setValue("Adiós, María!");

        assertEquals(config, tvm.getConfig());
        assertEquals(key, tvm.getKey());
        assertEquals("Adiós, María!", tvm.getValue());
    }
}
