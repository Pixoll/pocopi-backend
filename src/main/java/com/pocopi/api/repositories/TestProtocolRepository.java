package com.pocopi.api.repositories;

import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestProtocolModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestProtocolRepository extends JpaRepository<TestProtocolModel, Integer> {
    TestProtocolModel getById(int id);

    TestProtocolModel findByGroup(TestGroupModel group);

    List<TestProtocolModel> findAllByConfigVersion(int configVersion);
}
