package com.pocopi.api.repositories;

import com.pocopi.api.models.config.HomeFaqModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeFaqRepository extends JpaRepository<HomeFaqModel, Integer> {
    List<HomeFaqModel> findAllByConfigVersion(int configVersion);
}
