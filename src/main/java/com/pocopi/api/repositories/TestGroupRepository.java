package com.pocopi.api.repositories;

import com.pocopi.api.models.test.TestGroupModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestGroupRepository extends JpaRepository<TestGroupModel, Integer> {
    List<TestGroupModel> findAllByConfigVersion(int configVersion);
}
