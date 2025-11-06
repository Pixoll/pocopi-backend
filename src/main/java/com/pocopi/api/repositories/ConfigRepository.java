package com.pocopi.api.repositories;

import com.pocopi.api.models.config.ConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigModel, String> {
    @Query(
        value = "select * from config c where c.version = (select max(c2.version) from config c2)",
        nativeQuery = true
    )
    ConfigModel findLastConfig();
}
