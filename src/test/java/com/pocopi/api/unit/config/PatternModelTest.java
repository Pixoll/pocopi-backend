package com.pocopi.api.unit.config;

import com.pocopi.api.models.config.PatternModel;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PatternModelTest {

    @Test
    void testPatternModelBuilderAndFields() {
        String name = "digits";
        String regex = "\\d+";

        PatternModel patternModel = PatternModel.builder()
                .name(name)
                .regex(regex)
                .build();

        assertEquals(name, patternModel.getName());
        assertEquals(regex, patternModel.getRegex());
        assertEquals(0, patternModel.getId());
    }

    @Test
    void testGetPatternCompilesRegex() {
        PatternModel patternModel = PatternModel.builder()
                .name("letters")
                .regex("[a-zA-Z]+")
                .build();

        Pattern pattern = patternModel.getPattern();
        assertNotNull(pattern);
        assertEquals("[a-zA-Z]+", pattern.pattern());

        assertSame(pattern, patternModel.getPattern());
    }
}
