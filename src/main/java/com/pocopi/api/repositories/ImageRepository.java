package com.pocopi.api.repositories;

import com.pocopi.api.models.image.ImageModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<ImageModel, Integer> {
    ImageModel findByPath(String path);
}
