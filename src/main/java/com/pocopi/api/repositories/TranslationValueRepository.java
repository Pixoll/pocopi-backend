package com.pocopi.api.repositories;

import com.pocopi.api.models.config.TranslationValueModel;
import com.pocopi.api.repositories.projections.TranslationProjection;
import com.pocopi.api.repositories.projections.TranslationWithDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslationValueRepository extends JpaRepository<TranslationValueModel, Integer> {
    @NativeQuery(
        """
            select k.`key`, v.value, k.description, k.arguments as arguments_json
                from translation_key            k
                    left join translation_value v on k.id = v.key_id and v.config_version = :configVersion
            """
    )
    List<TranslationWithDetailsProjection> findAllByConfigVersionWithDetails(int configVersion);

    @NativeQuery(
        """
            select k.`key`, v.value
                from translation_key            k
                    left join translation_value v on k.id = v.key_id and v.config_version = :configVersion
            """
    )
    List<TranslationProjection> findAllKeyValuePairsByConfigVersion(int configVersion);

    List<TranslationValueModel> findAllByConfigVersion(int configVersion);
}
