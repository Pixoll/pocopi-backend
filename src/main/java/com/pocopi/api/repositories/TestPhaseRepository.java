package com.pocopi.api.repositories;

import com.pocopi.api.models.test.TestPhaseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestPhaseRepository extends JpaRepository<TestPhaseModel, Integer> {
    List<TestPhaseModel> findAllByGroupConfigVersionOrderByOrder(int groupConfigVersion);

    List<TestPhaseModel> findAllByGroupIdOrderByOrder(int groupId);
}
