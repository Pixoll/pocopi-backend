package com.pocopi.api.integration.config;

import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.ImageRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("integration")
class ImageIT {

    private static final Logger log = LoggerFactory.getLogger(ImageIT.class);

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @Transactional
    void createAndReadImages() {
        log.info("----------- Iniciando ImageIT.createAndReadImages -----------");

        // Construimos varias imagenes validas
        ImageModel img1 = ImageModel.builder()
            .path("/images/integration/image1.png")
            .alt("Imagen de integración 1")
            .build();

        ImageModel img2 = ImageModel.builder()
            .path("/images/integration/image2.jpg")
            .alt("Imagen de integración 2")
            .build();

        ImageModel img3 = ImageModel.builder()
            .path("/images/integration/image3.webp")
            .alt(null)
            .build();

        // Alamcenamos las imagenes en al db
        ImageModel saved1 = imageRepository.save(img1);
        ImageModel saved2 = imageRepository.save(img2);
        ImageModel saved3 = imageRepository.save(img3);

        log.info("Imágenes guardadas con ids: {}, {}, {}",
            saved1.getId(), saved2.getId(), saved3.getId());

        // Verififcamos la generacion de IDs
        assertTrue(saved1.getId() > 0, "img1 debe tener id generado");
        assertTrue(saved2.getId() > 0, "img2 debe tener id generado");
        assertTrue(saved3.getId() > 0, "img3 debe tener id generado");

        // Verificamos que las imagenes se encuentran almacenadas dado su ID
        Optional<ImageModel> found1 = imageRepository.findById(saved1.getId());
        assertTrue(found1.isPresent(), "Debe encontrarse img1 por id");
        assertEquals("/images/integration/image1.png", found1.get().getPath());

        Optional<ImageModel> found3 = imageRepository.findById(saved3.getId());
        assertTrue(found3.isPresent(), "Debe encontrarse img3 por id");
        assertNull(found3.get().getAlt(), "alt de img3 debe ser null");

        // Lectura de todas las imágenes
        List<ImageModel> all = imageRepository.findAll();
        assertTrue(
            all.stream().anyMatch(i -> "/images/integration/image1.png".equals(i.getPath())),
            "findAll debe incluir img1"
        );
        assertTrue(
            all.stream().anyMatch(i -> "/images/integration/image2.jpg".equals(i.getPath())),
            "findAll debe incluir img2"
        );
        assertTrue(
            all.stream().anyMatch(i -> "/images/integration/image3.webp".equals(i.getPath())),
            "findAll debe incluir img3"
        );

        log.info("----------- Finalizó correctamente ImageIT.createAndReadImages -----------");
    }
}
