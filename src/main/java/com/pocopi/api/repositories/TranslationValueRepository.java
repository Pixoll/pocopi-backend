package com.pocopi.api.repositories;

import com.pocopi.api.dto.translation.Translation;
import com.pocopi.api.models.config.TranslationValueModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslationValueRepository extends JpaRepository<TranslationValueModel, Integer> {
    @NativeQuery(
        """
            select k.`key`, v.value
                 from translation_value   v
                     join translation_key k on k.id = v.key_id
                 where v.config_version = :configVersion
            """
    )
    List<Translation> findAllByConfigVersion(int configVersion);
}
