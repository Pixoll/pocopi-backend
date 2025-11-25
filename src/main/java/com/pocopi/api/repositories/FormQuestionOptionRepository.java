package com.pocopi.api.repositories;

import com.pocopi.api.models.form.FormQuestionOptionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormQuestionOptionRepository extends JpaRepository<FormQuestionOptionModel, Integer> {
    Optional<FormQuestionOptionModel> findByIdAndFormQuestionId(int id, int formQuestionId);

    List<FormQuestionOptionModel> findAllByFormQuestionFormConfigVersionOrderByOrder(int formQuestionFormConfigVersion);

    List<FormQuestionOptionModel> findAllByFormQuestionFormIdOrderByOrder(int formQuestionFormId);

    List<FormQuestionOptionModel> findAllByFormQuestionId(int formQuestionId);
}
