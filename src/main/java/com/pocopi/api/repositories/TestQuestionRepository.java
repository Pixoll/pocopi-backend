package com.pocopi.api.repositories;

import com.pocopi.api.models.test.TestQuestionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestQuestionRepository extends JpaRepository<TestQuestionModel, Integer> {
    Optional<TestQuestionModel> findByIdAndPhaseGroupId(int id, int phaseGroupId);

    List<TestQuestionModel> findAllByPhaseId(int phaseId);

    List<TestQuestionModel> findAllByPhaseGroupConfigVersionOrderByOrder(int phaseGroupConfigVersion);

    List<TestQuestionModel> findAllByPhaseGroupIdOrderByOrder(int phaseGroupId);
}
