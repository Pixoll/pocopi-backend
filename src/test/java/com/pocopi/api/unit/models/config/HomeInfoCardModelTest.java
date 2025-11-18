package com.pocopi.api.unit.models.config;

import com.pocopi.api.models.config.HomeInfoCardModel;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HomeInfoCardModelTest {

    @Test
    void testBuilderAndFields() {
        ConfigModel config = ConfigModel.builder()
                .title("Config Title")
                .description("Config Description")
                .informedConsent("Consent Text")
                .build();

        ImageModel icon = ImageModel.builder()
                .path("/img/icon.png")
                .alt("Info Icon")
                .build();

        byte order = 3;
        String title = "Card Title";
        String description = "Short description.";
        Integer color = 0x123456;

        HomeInfoCardModel card = HomeInfoCardModel.builder()
                .config(config)
                .order(order)
                .title(title)
                .description(description)
                .icon(icon)
                .color(color)
                .build();

        assertEquals(config, card.getConfig());
        assertEquals(order, card.getOrder());
        assertEquals(title, card.getTitle());
        assertEquals(description, card.getDescription());
        assertEquals(icon, card.getIcon());
        assertEquals(color, card.getColor());
        assertEquals(0, card.getId());
    }

    @Test
    void testDefaults() {
        HomeInfoCardModel card = HomeInfoCardModel.builder()
                .config(ConfigModel.builder()
                        .title("Dummy")
                        .description("D")
                        .informedConsent("C")
                        .build())
                .order((byte)0)
                .title("Default Test")
                .description("D")
                .build();

        assertNull(card.getIcon());
        assertNull(card.getColor());
    }
}
