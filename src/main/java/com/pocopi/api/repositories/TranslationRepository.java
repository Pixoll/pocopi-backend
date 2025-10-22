package com.pocopi.api.repositories;

import com.pocopi.api.models.config.TranslationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslationRepository extends JpaRepository<TranslationModel, Integer> {
    @Query(value = "select * from translation as t where t.config_version = :configVersion", nativeQuery = true)
    List<TranslationModel> findAllByConfigVersion(int configVersion);
}
