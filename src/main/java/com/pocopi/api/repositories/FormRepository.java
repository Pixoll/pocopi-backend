package com.pocopi.api.repositories;

import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.form.FormType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<FormModel, Integer> {
    List<FormModel> findAllByConfigVersion(int configVersion);

    Optional<FormModel> findByTypeAndConfigVersion(FormType type, int configVersion);
}
