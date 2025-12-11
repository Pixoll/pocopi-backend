package com.pocopi.api.unit.models.test;

import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestQuestionModelTest {

    @Test
    void testBuilderAndFields() {
        ConfigModel config = ConfigModel.builder()
                .title("Config")
                .description("For questions")
                .informedConsent("Consent")
                .build();

        TestGroupModel group = TestGroupModel.builder()
                .config(config)
                .label("Group 1")
                .probability((byte)50)
                .build();

        TestPhaseModel phase = TestPhaseModel.builder()
                .group(group)
                .order((byte)1)
                .build();

        ImageModel image = ImageModel.builder()
                .path("/img/q1.png")
                .alt("Question Image")
                .build();

        short order = 1;
        String text = "What is your name?";

        TestQuestionModel question = TestQuestionModel.builder()
                .phase(phase)
                .order(order)
                .text(text)
                .image(image)
                .randomizeOptions(true)
                .build();

        assertEquals(phase, question.getPhase());
        assertEquals(order, question.getOrder());
        assertEquals(text, question.getText());
        assertEquals(image, question.getImage());
        assertTrue(question.isRandomizeOptions());
        assertEquals(0, question.getId());
    }

    @Test
    void testDefaults() {
        TestPhaseModel phase = TestPhaseModel.builder()
                .group(TestGroupModel.builder()
                        .config(ConfigModel.builder()
                                .title("Default")
                                .description("D")
                                .informedConsent("C")
                                .build())
                        .label("G")
                        .probability((byte)10)
                        .build())
                .order((byte)3)
                .build();

        TestQuestionModel question = TestQuestionModel.builder()
                .phase(phase)
                .order((byte)2)
                .build();

        assertNull(question.getText());
        assertNull(question.getImage());
        assertFalse(question.isRandomizeOptions());
    }

    @Test
    void testSettersAndGetters() {
        TestQuestionModel question = new TestQuestionModel();
        TestPhaseModel phase = TestPhaseModel.builder()
                .group(TestGroupModel.builder()
                        .config(ConfigModel.builder()
                                .title("Setter Config")
                                .description("D")
                                .informedConsent("C")
                                .build())
                        .label("Setter")
                        .probability((byte)1)
                        .build())
                .order((byte)5)
                .build();

        ImageModel image = ImageModel.builder()
                .path("/img/img2.png")
                .alt("Alt image")
                .build();

        question.setPhase(phase);
        question.setOrder((byte)7);
        question.setText("How old are you?");
        question.setImage(image);
        question.setRandomizeOptions(true);

        assertEquals(phase, question.getPhase());
        assertEquals(7, question.getOrder());
        assertEquals("How old are you?", question.getText());
        assertEquals(image, question.getImage());
        assertTrue(question.isRandomizeOptions());
    }
}
