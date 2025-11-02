package com.pocopi.api.repositories;

import com.pocopi.api.models.config.HomeInfoCardModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeInfoCardRepository extends JpaRepository<HomeInfoCardModel, Integer> {
    List<HomeInfoCardModel> findAllByConfigVersion(int configVersion);
}
