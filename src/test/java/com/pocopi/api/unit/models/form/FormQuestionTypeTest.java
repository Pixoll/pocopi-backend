package com.pocopi.api.unit.models.form;

import com.pocopi.api.models.form.FormQuestionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormQuestionTypeTest {

    @Test
    void testFromValue() {
        assertEquals(FormQuestionType.SELECT_ONE, FormQuestionType.fromValue("select-one"));
        assertEquals(FormQuestionType.SELECT_MULTIPLE, FormQuestionType.fromValue("select-multiple"));
        assertEquals(FormQuestionType.SLIDER, FormQuestionType.fromValue("slider"));
        assertEquals(FormQuestionType.TEXT_SHORT, FormQuestionType.fromValue("text-short"));
        assertEquals(FormQuestionType.TEXT_LONG, FormQuestionType.fromValue("text-long"));
    }

    @Test
    void testFromValueThrowsOnInvalid() {
        assertThrows(IllegalArgumentException.class, () -> FormQuestionType.fromValue("radio"));
    }

    @Test
    void testJsonValueAndToString() {
        assertEquals("select-one", FormQuestionType.SELECT_ONE.getValue());
        assertEquals("select-multiple", FormQuestionType.SELECT_MULTIPLE.getValue());
        assertEquals("slider", FormQuestionType.SLIDER.getValue());
        assertEquals("text-short", FormQuestionType.TEXT_SHORT.getValue());
        assertEquals("text-long", FormQuestionType.TEXT_LONG.getValue());
        assertEquals("select-one", FormQuestionType.SELECT_ONE.toString());
        assertEquals("slider", FormQuestionType.SLIDER.toString());
    }
}
