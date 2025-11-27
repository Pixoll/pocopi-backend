package com.pocopi.api.unit.form;

import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.models.config.ConfigModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormModelTest {

    @Test
    void testBuilderAndFields() {
        ConfigModel config = ConfigModel.builder()
                .title("Survey Config")
                .description("Survey")
                .informedConsent("Consent")
                .build();

        String title = "Pre-Survey";
        FormType type = FormType.PRE;

        FormModel form = FormModel.builder()
                .config(config)
                .title(title)
                .type(type)
                .build();

        assertEquals(config, form.getConfig());
        assertEquals(title, form.getTitle());
        assertEquals(type, form.getType());
        assertEquals(0, form.getId());
    }

    @Test
    void testDefaultsAndSetters() {
        FormModel form = new FormModel();
        ConfigModel config = ConfigModel.builder()
                .title("Basic")
                .description("D")
                .informedConsent("C")
                .build();

        form.setConfig(config);
        form.setTitle("Post Survey");
        form.setType(FormType.POST);

        assertEquals(config, form.getConfig());
        assertEquals("Post Survey", form.getTitle());
        assertEquals(FormType.POST, form.getType());
    }
}
