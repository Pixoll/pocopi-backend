package com.pocopi.api.repositories;

import com.pocopi.api.models.form.FormModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormRepository extends JpaRepository<FormModel, Integer> {
    @Query(
        value = """
            select
                f.id as form_id,
                f.config_version,
                fq.id as question_id,
                fq.type as question_type_string,
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
            from form f
                left join form_question fq on f.id = fq.form_id
                left join image fi on fq.image_id = fi.id
                left join form_question_option fqo on fq.id = fqo.form_question_id
                left join image oi on fqo.image_id = oi.id
                left join form_question_slider_label sl on fq.id = sl.form_question_id
            where f.id = :formId
            order by fq.id, fqo.id, sl.label
            """,
        nativeQuery = true
    )
    List<FormProjection> findFormWithAllData(int formId);

    @Query(value = "select * from form as f where f.config_version = :configVersion", nativeQuery = true)
    List<FormModel> findAllByConfigVersion(int configVersion);
}
