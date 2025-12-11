package com.pocopi.api.unit.models.test;

import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.config.ConfigModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestGroupModelTest {

    @Test
    void testBuilderAndFields() {
        ConfigModel config = ConfigModel.builder()
                .title("A/B Test Config")
                .description("A test configuration")
                .informedConsent("Consent here")
                .build();

        String label = "Group A";
        byte probability = 75;
        String greeting = "Welcome to Group A!";

        TestGroupModel group = TestGroupModel.builder()
                .config(config)
                .label(label)
                .probability(probability)
                .greeting(greeting)
                .allowPreviousPhase(false)
                .allowPreviousQuestion(false)
                .allowSkipQuestion(false)
                .randomizePhases(true)
                .build();

        assertEquals(config, group.getConfig());
        assertEquals(label, group.getLabel());
        assertEquals(probability, group.getProbability());
        assertEquals(greeting, group.getGreeting());
        assertFalse(group.isAllowPreviousPhase());
        assertFalse(group.isAllowPreviousQuestion());
        assertFalse(group.isAllowSkipQuestion());
        assertTrue(group.isRandomizePhases());
        assertEquals(0, group.getId());
    }

    @Test
    void testDefaults() {
        TestGroupModel group = TestGroupModel.builder()
                .config(ConfigModel.builder()
                        .title("B")
                        .description("D")
                        .informedConsent("C")
                        .build())
                .label("Control")
                .probability((byte)50)
                .build();

        assertEquals("Control", group.getLabel());
        assertEquals(50, group.getProbability());
        assertNull(group.getGreeting());
        assertTrue(group.isAllowPreviousPhase());
        assertTrue(group.isAllowPreviousQuestion());
        assertTrue(group.isAllowSkipQuestion());
        assertFalse(group.isRandomizePhases());
    }

    @Test
    void testSettersAndGetters() {
        TestGroupModel group = new TestGroupModel();
        ConfigModel config = ConfigModel.builder()
                .title("Setter Test")
                .description("D")
                .informedConsent("C")
                .build();

        group.setConfig(config);
        group.setLabel("Experimental");
        group.setProbability((byte) 10);
        group.setGreeting("Hello!");
        group.setAllowPreviousPhase(true);
        group.setAllowPreviousQuestion(true);
        group.setAllowSkipQuestion(false);
        group.setRandomizePhases(true);

        assertEquals(config, group.getConfig());
        assertEquals("Experimental", group.getLabel());
        assertEquals(10, group.getProbability());
        assertEquals("Hello!", group.getGreeting());
        assertTrue(group.isAllowPreviousPhase());
        assertTrue(group.isAllowPreviousQuestion());
        assertFalse(group.isAllowSkipQuestion());
        assertTrue(group.isRandomizePhases());
    }
}
