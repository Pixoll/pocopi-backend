package com.pocopi.api.unit.models.test;

import com.pocopi.api.models.test.TestOptionEventType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestOptionEventTypeTest {

    @Test
    void testFromValueSelect() {
        TestOptionEventType type = TestOptionEventType.fromValue("select");
        assertEquals(TestOptionEventType.SELECT, type);
        assertEquals("select", type.getValue());
    }

    @Test
    void testFromValueDeselect() {
        TestOptionEventType type = TestOptionEventType.fromValue("deselect");
        assertEquals(TestOptionEventType.DESELECT, type);
        assertEquals("deselect", type.getValue());
    }

    @Test
    void testFromValueHover() {
        TestOptionEventType type = TestOptionEventType.fromValue("hover");
        assertEquals(TestOptionEventType.HOVER, type);
        assertEquals("hover", type.getValue());
    }

    @Test
    void testFromValueThrowsOnInvalid() {
        assertThrows(IllegalArgumentException.class, () -> TestOptionEventType.fromValue("click"));
    }

    @Test
    void testToString() {
        assertEquals("select", TestOptionEventType.SELECT.toString());
        assertEquals("deselect", TestOptionEventType.DESELECT.toString());
        assertEquals("hover", TestOptionEventType.HOVER.toString());
    }
}
