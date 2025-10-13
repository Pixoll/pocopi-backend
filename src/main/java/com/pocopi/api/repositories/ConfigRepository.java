package com.pocopi.api.repositories;

import com.pocopi.api.models.config.ConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigModel,String> {

    @Query(value = "SELECT * FROM config c WHERE c.version = (SELECT max(c2.version) FROM config c2)\n", nativeQuery = true)
    ConfigModel findLastConfig();

    ConfigModel getByVersion(int version);
}
