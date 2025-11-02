package com.pocopi.api.repositories;

import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestQuestionRepository extends JpaRepository<TestQuestionModel, Integer> {
    List<TestQuestionModel> findAllByPhase(TestPhaseModel phase);

    Optional<TestQuestionModel> findByIdAndPhaseProtocolConfigVersion(int id, int phaseProtocolConfigVersion);
}
