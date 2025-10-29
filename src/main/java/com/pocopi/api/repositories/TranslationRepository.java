package com.pocopi.api.repositories;

import com.pocopi.api.dto.translation.Translation;
import com.pocopi.api.models.config.TranslationValueModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslationRepository extends JpaRepository<TranslationValueModel, Integer> {
    @Query(value = """
        SELECT tk.`key` AS key_, tv.value AS value
        FROM translation_value tv
        JOIN translation_key tk ON tk.id = tv.key_id
        WHERE tv.config_version = :configVersion
        """,
        nativeQuery = true)
    List<Translation> findAllByConfigVersion(@Param("configVersion") int configVersion);
}
