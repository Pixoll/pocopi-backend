package com.pocopi.api.repositories;

import com.pocopi.api.models.form.UserFormSubmissionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFormSubmissionRepository extends JpaRepository<UserFormSubmissionModel, Long> {
}
