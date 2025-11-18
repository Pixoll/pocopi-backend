package com.pocopi.api.unit.models.test;

import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestOptionModelTest {

    @Test
    void testBuilderAndFields() {
        ConfigModel config = ConfigModel.builder()
                .title("Options Config")
                .description("Config for options")
                .informedConsent("Consent")
                .build();

        TestGroupModel group = TestGroupModel.builder()
                .config(config)
                .label("OptionGroup")
                .probability((byte)50)
                .build();

        TestPhaseModel phase = TestPhaseModel.builder()
                .group(group)
                .order((byte)3)
                .build();

        TestQuestionModel question = TestQuestionModel.builder()
                .phase(phase)
                .order((byte)1)
                .text("What is 2+2?")
                .build();

        ImageModel image = ImageModel.builder()
                .path("/img/option.png")
                .alt("Option image")
                .build();

        byte order = 2;
        String text = "Four";

        TestOptionModel option = TestOptionModel.builder()
                .question(question)
                .order(order)
                .text(text)
                .image(image)
                .correct(true)
                .build();

        assertEquals(question, option.getQuestion());
        assertEquals(order, option.getOrder());
        assertEquals(text, option.getText());
        assertEquals(image, option.getImage());
        assertTrue(option.isCorrect());
        assertEquals(0, option.getId());
    }

    @Test
    void testDefaults() {
        TestQuestionModel question = TestQuestionModel.builder()
                .phase(TestPhaseModel.builder()
                        .group(TestGroupModel.builder()
                                .config(ConfigModel.builder()
                                        .title("Default")
                                        .description("D")
                                        .informedConsent("C")
                                        .build())
                                .label("G")
                                .probability((byte)1)
                                .build())
                        .order((byte)1)
                        .build())
                .order((byte)1)
                .text("Default question")
                .build();

        TestOptionModel option = TestOptionModel.builder()
                .question(question)
                .order((byte)5)
                .build();

        assertNull(option.getText());
        assertNull(option.getImage());
        assertFalse(option.isCorrect());
    }

    @Test
    void testSettersAndGetters() {
        TestOptionModel option = new TestOptionModel();
        TestQuestionModel question = TestQuestionModel.builder()
                .phase(TestPhaseModel.builder()
                        .group(TestGroupModel.builder()
                                .config(ConfigModel.builder()
                                        .title("Setter config")
                                        .description("X")
                                        .informedConsent("Y")
                                        .build())
                                .label("Setter")
                                .probability((byte)3)
                                .build())
                        .order((byte)5)
                        .build())
                .order((byte)2)
                .text("How do you spell cat?")
                .build();

        ImageModel image = ImageModel.builder()
                .path("/img/img3.png")
                .alt("Alt Image")
                .build();

        option.setQuestion(question);
        option.setOrder((byte)6);
        option.setText("CAT");
        option.setImage(image);
        option.setCorrect(true);

        assertEquals(question, option.getQuestion());
        assertEquals(6, option.getOrder());
        assertEquals("CAT", option.getText());
        assertEquals(image, option.getImage());
        assertTrue(option.isCorrect());
    }
}
