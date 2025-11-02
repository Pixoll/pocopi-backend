package com.pocopi.api.repositories;

import com.pocopi.api.models.form.FormQuestionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormQuestionRepository extends JpaRepository<FormQuestionModel, Integer> {
    FormQuestionModel getFormQuestionModelById(int id);

    Optional<FormQuestionModel> findByIdAndFormId(int id, int formId);

    List<FormQuestionModel> findAllByFormConfigVersion(int formConfigVersion);
}
