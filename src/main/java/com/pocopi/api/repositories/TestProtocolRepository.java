package com.pocopi.api.repositories;

import com.pocopi.api.models.TestGroupModel;
import com.pocopi.api.models.TestProtocolModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestProtocolRepository extends JpaRepository<TestProtocolModel, Integer> {
    TestProtocolModel getById(Integer id);

    TestProtocolModel findByGroup(TestGroupModel group);
}
