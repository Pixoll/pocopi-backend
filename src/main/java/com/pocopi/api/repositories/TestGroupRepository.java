package com.pocopi.api.repositories;

import com.pocopi.api.models.TestGroupModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestGroupRepository extends JpaRepository<TestGroupModel, Integer> {
}
