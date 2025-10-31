package com.pocopi.api.repositories;

import com.pocopi.api.models.config.TranslationValueModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslationValueRepository extends JpaRepository<TranslationValueModel, Integer> {
}
