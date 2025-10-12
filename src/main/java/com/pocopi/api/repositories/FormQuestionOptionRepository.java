package com.pocopi.api.repositories;

import com.pocopi.api.models.FormQuestionOptionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormQuestionOptionRepository extends JpaRepository<FormQuestionOptionModel, Integer> {
    FormQuestionOptionModel getFormQuestionOptionModelById(int id);

    List<FormQuestionOptionModel> findAllByFormQuestion_Id(int formQuestionId);
}
