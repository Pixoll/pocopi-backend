package com.pocopi.api.unit.models.test;

import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.config.ConfigModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestPhaseModelTest {

    @Test
    void testBuilderAndFields() {
        ConfigModel config = ConfigModel.builder()
                .title("Experiment")
                .description("Config Desc")
                .informedConsent("Consent Text")
                .build();

        TestGroupModel group = TestGroupModel.builder()
                .config(config)
                .label("A")
                .probability((byte)80)
                .build();

        byte order = 2;

        TestPhaseModel phase = TestPhaseModel.builder()
                .group(group)
                .order(order)
                .randomizeQuestions(true)
                .build();

        assertEquals(group, phase.getGroup());
        assertEquals(order, phase.getOrder());
        assertTrue(phase.isRandomizeQuestions());
        assertEquals(0, phase.getId());
    }

    @Test
    void testDefaults() {
        TestGroupModel group = TestGroupModel.builder()
                .config(ConfigModel.builder()
                        .title("Default")
                        .description("D")
                        .informedConsent("C")
                        .build())
                .label("B")
                .probability((byte)40)
                .build();

        TestPhaseModel phase = TestPhaseModel.builder()
                .group(group)
                .order((byte)1)
                .build();

        assertFalse(phase.isRandomizeQuestions());
    }

    @Test
    void testSettersAndGetters() {
        TestPhaseModel phase = new TestPhaseModel();
        TestGroupModel group = TestGroupModel.builder()
                .config(ConfigModel.builder()
                        .title("Setter Test")
                        .description("X")
                        .informedConsent("Y")
                        .build())
                .label("Setter")
                .probability((byte)10)
                .build();

        phase.setGroup(group);
        phase.setOrder((byte)7);
        phase.setRandomizeQuestions(true);

        assertEquals(group, phase.getGroup());
        assertEquals(7, phase.getOrder());
        assertTrue(phase.isRandomizeQuestions());
    }
}
