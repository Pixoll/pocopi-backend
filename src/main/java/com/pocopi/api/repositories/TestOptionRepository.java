package com.pocopi.api.repositories;

import com.pocopi.api.models.test.TestOptionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestOptionRepository extends JpaRepository<TestOptionModel, Integer> {
    List<TestOptionModel> findAllByQuestionId(int questionId);

    Optional<TestOptionModel> findByIdAndQuestionPhaseGroupId(int id, int questionPhaseGroupId);

    List<TestOptionModel> findAllByQuestionPhaseGroupConfigVersionOrderByOrder(int questionPhaseGroupConfigVersion);

    List<TestOptionModel> findAllByQuestionPhaseGroupIdOrderByOrder(int questionPhaseGroupId);
}
