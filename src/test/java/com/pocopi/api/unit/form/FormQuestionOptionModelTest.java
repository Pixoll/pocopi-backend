package com.pocopi.api.unit.form;

import com.pocopi.api.models.form.*;
import com.pocopi.api.models.config.ImageModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormQuestionOptionModelTest {

    @Test
    void testBuilderAndFields() {
        FormModel form = FormModel.builder()
                .title("Survey")
                .type(FormType.POST)
                .build();

        FormQuestionModel question = FormQuestionModel.builder()
                .form(form)
                .order((byte)1)
                .category("Choices")
                .type(FormQuestionType.SELECT_ONE)
                .build();

        ImageModel image = ImageModel.builder()
                .path("/img/option.png")
                .alt("Option")
                .build();

        byte order = 2;
        String text = "Option Text";

        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
                .formQuestion(question)
                .order(order)
                .text(text)
                .image(image)
                .build();

        assertEquals(question, option.getFormQuestion());
        assertEquals(order, option.getOrder());
        assertEquals(text, option.getText());
        assertEquals(image, option.getImage());
        assertEquals(0, option.getId());
    }

    @Test
    void testDefaults() {
        FormModel form = FormModel.builder()
                .title("Default")
                .type(FormType.POST)
                .build();

        FormQuestionModel question = FormQuestionModel.builder()
                .form(form)
                .order((byte)0)
                .category("Default Q")
                .type(FormQuestionType.SELECT_ONE)
                .build();

        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
                .formQuestion(question)
                .order((byte)0)
                .build();

        assertNull(option.getText());
        assertNull(option.getImage());
    }

    @Test
    void testSettersAndGetters() {
        FormQuestionOptionModel option = new FormQuestionOptionModel();
        FormQuestionModel question = FormQuestionModel.builder()
                .form(FormModel.builder()
                        .title("QForm")
                        .type(FormType.PRE)
                        .build())
                .order((byte)3)
                .category("SetterCat")
                .type(FormQuestionType.SELECT_ONE)
                .build();

        ImageModel image = ImageModel.builder()
                .path("/img/imgopt.png")
                .alt("Setter Alt")
                .build();

        option.setFormQuestion(question);
        option.setOrder((byte)7);
        option.setText("SetText");
        option.setImage(image);

        assertEquals(question, option.getFormQuestion());
        assertEquals(7, option.getOrder());
        assertEquals("SetText", option.getText());
        assertEquals(image, option.getImage());
    }
}
