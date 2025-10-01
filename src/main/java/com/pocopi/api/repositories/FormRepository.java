package com.pocopi.api.repositories;

import com.pocopi.api.models.FormModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormRepository extends JpaRepository<FormModel, Integer> {

    @Query(value = """
        SELECT
        f.id as form_id,
        f.config_version,
        fq.id as question_id,
        fq.type as question_type,
        fq.category,
        fq.text as question_text,
        fq.placeholder,
        fq.min,
        fq.max,
        fq.step,
        fq.min_length,
        fq.max_length,
        fq.other,
        fi.path as question_image_path,
        fqo.id as option_id,
        fqo.text as option_text,
        oi.path as option_image_path,
        sl.number as slider_value,
        sl.label as slider_label
        FROM form f
             LEFT JOIN form_question fq ON f.id = fq.form_id
             LEFT JOIN image fi ON fq.image_id = fi.id
             LEFT JOIN form_question_option fqo ON fq.id = fqo.form_question_id
             LEFT JOIN image oi ON fqo.image_id = oi.id
             LEFT JOIN form_question_slider_label sl ON fq.id = sl.form_question_id
        WHERE f.id = :formId
        ORDER BY fq.id, fqo.id, sl.label
        """, nativeQuery = true)
    List<FormProjection> findFormWithAllData(int formId);

    @Query(value = "SELECT * FROM form as f WHERE f.config_version =:configVersion",nativeQuery=true)
    List<FormModel> findAllByConfigVersion(int configVersion);
}