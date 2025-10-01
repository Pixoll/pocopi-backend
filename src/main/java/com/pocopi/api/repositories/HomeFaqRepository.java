package com.pocopi.api.repositories;

import com.pocopi.api.models.HomeFaqModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeFaqRepository extends JpaRepository<HomeFaqModel, Integer> {

    @Query(value = "SELECT * from home_faq as h where h.config_version =:configVersion", nativeQuery = true)
    List<HomeFaqModel> findAllByConfigVersion(int configVersion);
}
