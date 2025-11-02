package com.pocopi.api.repositories;

import com.pocopi.api.models.form.FormModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormRepository extends JpaRepository<FormModel, Integer> {
    List<FormModel> findAllByConfigVersion(int configVersion);
}
