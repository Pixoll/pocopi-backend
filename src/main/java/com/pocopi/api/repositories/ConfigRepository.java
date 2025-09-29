package com.pocopi.api.repositories;

import com.pocopi.api.models.ConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigModel,String> {

    @Query(value = "SELECT * from config",nativeQuery = true)
    List<ConfigModel> findAll();
}
