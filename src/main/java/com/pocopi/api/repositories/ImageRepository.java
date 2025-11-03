package com.pocopi.api.repositories;

import com.pocopi.api.models.config.ImageModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageModel, Integer> {
    Optional<ImageModel> findByPath(String path);
}
