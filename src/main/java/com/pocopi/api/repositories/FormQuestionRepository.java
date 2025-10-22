package com.pocopi.api.repositories;

import com.pocopi.api.models.form.FormQuestionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormQuestionRepository extends JpaRepository<FormQuestionModel, Integer> {
    FormQuestionModel getFormQuestionModelById(int id);
}
