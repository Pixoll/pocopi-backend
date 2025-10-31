package com.pocopi.api.repositories;

import com.pocopi.api.models.config.TranslationKeyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslationKeyRepository extends JpaRepository<TranslationKeyModel, Integer> {
    TranslationKeyModel getByKey(String key);
}
