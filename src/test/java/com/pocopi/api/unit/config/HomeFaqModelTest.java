package com.pocopi.api.unit.config;

import com.pocopi.api.models.config.HomeFaqModel;
import com.pocopi.api.models.config.ConfigModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HomeFaqModelTest {

    @Test
    void testBuilderAndFields() {
        ConfigModel config = ConfigModel.builder()
                .title("FAQ Config")
                .description("Description")
                .informedConsent("Consent")
                .build();

        byte order = 1;
        String question = "What is this app?";
        String answer = "This app is a demo.";

        HomeFaqModel faq = HomeFaqModel.builder()
                .config(config)
                .order(order)
                .question(question)
                .answer(answer)
                .build();

        assertEquals(config, faq.getConfig());
        assertEquals(order, faq.getOrder());
        assertEquals(question, faq.getQuestion());
        assertEquals(answer, faq.getAnswer());
        assertEquals(0, faq.getId());
    }

    @Test
    void testSettersAndGetters() {
        HomeFaqModel faq = new HomeFaqModel();
        ConfigModel config = ConfigModel.builder()
                .title("Another Config")
                .description("Desc")
                .informedConsent("Text")
                .build();

        faq.setConfig(config);
        faq.setOrder((byte)2);
        faq.setQuestion("How to use?");
        faq.setAnswer("Just read the docs.");

        assertEquals(config, faq.getConfig());
        assertEquals(2, faq.getOrder());
        assertEquals("How to use?", faq.getQuestion());
        assertEquals("Just read the docs.", faq.getAnswer());
    }
}
