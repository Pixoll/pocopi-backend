package com.pocopi.api.repositories;

import com.pocopi.api.models.TestOptionModel;
import com.pocopi.api.models.TestQuestionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestOptionRepository extends JpaRepository<TestOptionModel, Integer> {
    List<TestOptionModel> findAllByQuestion(TestQuestionModel question);
}
