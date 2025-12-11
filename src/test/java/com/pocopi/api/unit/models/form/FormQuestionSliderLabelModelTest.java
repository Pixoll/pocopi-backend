package com.pocopi.api.unit.models.form;

import com.pocopi.api.models.form.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormQuestionSliderLabelModelTest {

    @Test
    void testBuilderAndFields() {
        FormModel form = FormModel.builder()
                .title("SliderForm")
                .type(FormType.POST)
                .build();

        FormQuestionModel question = FormQuestionModel.builder()
                .form(form)
                .order((byte)1)
                .category("SliderCat")
                .type(FormQuestionType.SLIDER)
                .build();

        int number = 5;
        String labelText = "Near Maximum";

        FormQuestionSliderLabelModel label = FormQuestionSliderLabelModel.builder()
                .formQuestion(question)
                .number(number)
                .label(labelText)
                .build();

        assertEquals(question, label.getFormQuestion());
        assertEquals(number, label.getNumber());
        assertEquals(labelText, label.getLabel());
        assertEquals(0, label.getId());
    }

    @Test
    void testSettersAndGetters() {
        FormQuestionSliderLabelModel label = new FormQuestionSliderLabelModel();
        FormQuestionModel question = FormQuestionModel.builder()
                .form(FormModel.builder()
                        .title("LabelQForm")
                        .type(FormType.PRE)
                        .build())
                .order((byte)2)
                .category("LabelCat")
                .type(FormQuestionType.SLIDER)
                .build();

        int number = 3;
        String text = "Middle";

        label.setFormQuestion(question);
        label.setNumber(number);
        label.setLabel(text);

        assertEquals(question, label.getFormQuestion());
        assertEquals(3, label.getNumber());
        assertEquals("Middle", label.getLabel());
    }
}
