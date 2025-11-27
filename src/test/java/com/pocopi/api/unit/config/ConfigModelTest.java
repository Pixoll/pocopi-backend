package com.pocopi.api.unit.config;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.config.PatternModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigModelTest {

    @Test
    void testBuilderAndFields() {
        ImageModel icon = ImageModel.builder()
                .path("icon_path.png")
                .alt("Main Icon")
                .build();

        PatternModel pattern = PatternModel.builder()
                .name("username pattern")
                .regex("[a-zA-Z0-9]+")
                .build();

        String title = "Main Title";
        String subtitle = "Subtitle";
        String description = "Description of the config.";
        String informedConsent = "Consent document text.";

        ConfigModel config = ConfigModel.builder()
                .icon(icon)
                .title(title)
                .subtitle(subtitle)
                .description(description)
                .informedConsent(informedConsent)
                .anonymous(false)
                .usernamePattern(pattern)
                .active(true)
                .build();

        assertEquals(icon, config.getIcon());
        assertEquals(title, config.getTitle());
        assertEquals(subtitle, config.getSubtitle());
        assertEquals(description, config.getDescription());
        assertEquals(informedConsent, config.getInformedConsent());
        assertFalse(config.isAnonymous());
        assertEquals(pattern, config.getUsernamePattern());
        assertTrue(config.isActive());
        assertEquals(0, config.getVersion());
    }

    @Test
    void testDefaults() {
        String title = "Default Config";
        String description = "Default Description";
        String informedConsent = "Default Consent";

        ConfigModel config = ConfigModel.builder()
                .title(title)
                .description(description)
                .informedConsent(informedConsent)
                .build();

        assertNull(config.getIcon());
        assertNull(config.getSubtitle());
        assertNull(config.getUsernamePattern());
        assertTrue(config.isAnonymous());
        assertTrue(config.isActive());
    }
}
