package com.pocopi.api.repositories;

import com.pocopi.api.models.FormQuestionOptionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormQuestionOptionRepository extends JpaRepository<FormQuestionOptionModel, Integer> {
}