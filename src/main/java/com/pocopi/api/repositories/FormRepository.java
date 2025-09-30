package com.pocopi.api.repositories;

import com.pocopi.api.models.FormModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormRepository extends JpaRepository<FormModel,Integer> {

}
