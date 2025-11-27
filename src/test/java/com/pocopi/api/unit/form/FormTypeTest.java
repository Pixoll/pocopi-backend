package com.pocopi.api.unit.form;

import com.pocopi.api.models.form.FormType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormTypeTest {

    @Test
    void testFromValuePre() {
        FormType type = FormType.fromValue("pre");
        assertEquals(FormType.PRE, type);
        assertEquals("pre", type.getValue());
    }

    @Test
    void testFromValuePost() {
        FormType type = FormType.fromValue("post");
        assertEquals(FormType.POST, type);
        assertEquals("post", type.getValue());
    }

    @Test
    void testFromValueThrowsOnInvalid() {
        assertThrows(IllegalArgumentException.class, () -> FormType.fromValue("unknown"));
    }

    @Test
    void testJsonValueAndToString() {
        assertEquals("pre", FormType.PRE.getValue());
        assertEquals("post", FormType.POST.getValue());
        assertEquals("pre", FormType.PRE.toString());
        assertEquals("post", FormType.POST.toString());
    }
}
