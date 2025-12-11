package com.pocopi.api.integration.repositories.config;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.ImageRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class ConfigIT {

    private static final Logger log = LoggerFactory.getLogger(ConfigIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @Transactional
    void createConfigsWithAndWithoutIcon() {
        log.info("----------- Iniciando ConfigIT.createConfigsWithAndWithoutIcon -----------");

        // 1) Creamos una imagen para usar como icono opcional
        ImageModel icon = ImageModel.builder()
            .path("/images/icon-config-1.png")
            .alt("Integration icon 1")
            .build();

        ImageModel savedIcon = imageRepository.save(icon);
        log.info("Icono guardado con id={} y path={}", savedIcon.getId(), savedIcon.getPath());
        assertNotNull(savedIcon.getId(), "La imagen debe tener id generado");

        // 2) Config sin icono
        ConfigModel configWithoutIcon = ConfigModel.builder()
            .title("Config sin icono")
            .subtitle("Subtítulo de prueba sin icono")
            .description("Descripción de prueba para config sin icono")
            .informedConsent("Consentimiento informado para config sin icono")
            .anonymous(true)
            .build();

        // 3) Config con icono
        ConfigModel configWithIcon = ConfigModel.builder()
            .icon(savedIcon)
            .title("Config con icono")
            .subtitle("Subtítulo de prueba con icono")
            .description("Descripción de prueba para config con icono")
            .informedConsent("Consentimiento informado para config con icono")
            .anonymous(false)
            .build();

        ConfigModel savedWithoutIcon = configRepository.save(configWithoutIcon);
        ConfigModel savedWithIcon = configRepository.save(configWithIcon);

        log.info("Configs guardadas: version(sinIcon)={}, version(conIcon)={}",
            savedWithoutIcon.getVersion(), savedWithIcon.getVersion());

        // IDs/version generados
        assertNotNull(savedWithoutIcon.getVersion(), "La config sin icono debe tener version generada");
        assertNotNull(savedWithIcon.getVersion(), "La config con icono debe tener version generada");

        // 4) Leer desde DB y verificar relaciones y campos;
        ConfigModel fetchedWithoutIcon =
            configRepository.findById(String.valueOf(savedWithoutIcon.getVersion())).orElseThrow();
        ConfigModel fetchedWithIcon =
            configRepository.findById(String.valueOf(savedWithIcon.getVersion())).orElseThrow();

        // Config sin icono: icon es null y campos obligatorios respetados
        assertNull(fetchedWithoutIcon.getIcon(), "La config sin icono no debe tener icon asociado");
        assertEquals("Config sin icono", fetchedWithoutIcon.getTitle());
        assertTrue(fetchedWithoutIcon.isAnonymous(), "La config sin icono debe ser anonymous=true");

        // Config con icono: icon no null y apunta a la imagen creada
        assertNotNull(fetchedWithIcon.getIcon(), "La config con icono debe tener icon asociado");
        assertEquals(savedIcon.getId(), fetchedWithIcon.getIcon().getId(),
            "El icono asociado debe ser el que se creó al inicio");
        assertEquals("Config con icono", fetchedWithIcon.getTitle());
        assertFalse(fetchedWithIcon.isAnonymous(), "La config con icono debe ser anonymous=false");

        log.info("----------- Finalizó correctamente ConfigIT.createConfigsWithAndWithoutIcon -----------");
    }
}
