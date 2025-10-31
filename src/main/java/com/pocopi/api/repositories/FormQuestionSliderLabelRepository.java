package com.pocopi.api.repositories;

import com.pocopi.api.models.form.FormQuestionSliderLabelModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormQuestionSliderLabelRepository extends JpaRepository<FormQuestionSliderLabelModel, Integer> {
}
