package com.pocopi.api.unit.models.config;

import com.pocopi.api.models.config.ImageModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageModelTest {

    @Test
    void testImageModelBuilderAndGetters() {
        String path = "/images/pic1.png";
        String alt = "Profile Picture";

        ImageModel img = ImageModel.builder()
                .path(path)
                .alt(alt)
                .build();

        assertEquals(path, img.getPath());
        assertEquals(alt, img.getAlt());
        assertEquals(0, img.getId());
    }

    @Test
    void testAltDefaultIsNull() {
        ImageModel img = ImageModel.builder()
                .path("/images/pic2.jpg")
                .build();

        assertNull(img.getAlt());
    }
}
