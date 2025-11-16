package com.pocopi.api.repositories;

import com.pocopi.api.models.config.PatternModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatternRepository extends JpaRepository<PatternModel, Integer> {
}
