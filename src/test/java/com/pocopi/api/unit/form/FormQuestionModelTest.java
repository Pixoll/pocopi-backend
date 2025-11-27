package com.pocopi.api.unit.form;

import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionType;
import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.config.ImageModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormQuestionModelTest {

    @Test
    void testBuilderAndFields() {
        FormModel form = FormModel.builder()
                .title("Survey")
                .type(null)
                .build();

        ImageModel image = ImageModel.builder()
                .path("/img/question.png")
                .alt("Question image")
                .build();

        FormQuestionModel q = FormQuestionModel.builder()
                .form(form)
                .order((byte)1)
                .category("General")
                .text("Pick a number:")
                .image(image)
                .required(false)
                .min(1)
                .max(10)
                .step(1)
                .other(true)
                .minLength(2)
                .maxLength(9)
                .placeholder("Example: 5")
                .type(FormQuestionType.SLIDER)
                .build();

        assertEquals(form, q.getForm());
        assertEquals(1, q.getOrder());
        assertEquals("General", q.getCategory());
        assertEquals("Pick a number:", q.getText());
        assertEquals(image, q.getImage());
        assertFalse(q.isRequired());
        assertEquals(1, q.getMin());
        assertEquals(10, q.getMax());
        assertEquals(1, q.getStep());
        assertTrue(q.getOther());
        assertEquals(2, q.getMinLength());
        assertEquals(9, q.getMaxLength());
        assertEquals("Example: 5", q.getPlaceholder());
        assertEquals(FormQuestionType.SLIDER, q.getType());
        assertEquals(0, q.getId());
    }

    @Test
    void testDefaults() {
        FormModel form = FormModel.builder()
                .title("Default Form")
                .type(null)
                .build();

        FormQuestionModel q = FormQuestionModel.builder()
                .form(form)
                .order((byte)0)
                .category("Default")
                .type(FormQuestionType.TEXT_SHORT)
                .build();

        assertNull(q.getText());
        assertNull(q.getImage());
        assertTrue(q.isRequired());
        assertNull(q.getMin());
        assertNull(q.getMax());
        assertNull(q.getStep());
        assertNull(q.getOther());
        assertNull(q.getMinLength());
        assertNull(q.getMaxLength());
        assertNull(q.getPlaceholder());
    }

    @Test
    void testSettersAndGetters() {
        FormQuestionModel q = new FormQuestionModel();
        FormModel form = FormModel.builder()
                .title("SetterForm")
                .type(null)
                .build();

        ImageModel image = ImageModel.builder()
                .path("img/xyz.png")
                .alt("Setter Alt")
                .build();

        q.setForm(form);
        q.setOrder((byte)5);
        q.setCategory("TestCat");
        q.setText("How are you?");
        q.setImage(image);
        q.setRequired(false);
        q.setMin(100);
        q.setMax(200);
        q.setStep(50);
        q.setOther(false);
        q.setMinLength(3);
        q.setMaxLength(8);
        q.setPlaceholder("Write here");
        q.setType(FormQuestionType.TEXT_SHORT);

        assertEquals(form, q.getForm());
        assertEquals(5, q.getOrder());
        assertEquals("TestCat", q.getCategory());
        assertEquals("How are you?", q.getText());
        assertEquals(image, q.getImage());
        assertFalse(q.isRequired());
        assertEquals(100, q.getMin());
        assertEquals(200, q.getMax());
        assertEquals(50, q.getStep());
        assertFalse(q.getOther());
        assertEquals(3, q.getMinLength());
        assertEquals(8, q.getMaxLength());
        assertEquals("Write here", q.getPlaceholder());
        assertEquals(FormQuestionType.TEXT_SHORT, q.getType());
    }
}
