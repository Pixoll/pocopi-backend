package com.pocopi.api.repositories;

import com.pocopi.api.models.form.UserFormSubmissionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFormSubmissionRepository extends JpaRepository<UserFormSubmissionModel, Long> {
    Optional<UserFormSubmissionModel> findByAttemptIdAndFormId(long attemptId, int formId);

    default boolean hasAnsweredForm(long attemptId, int formId) {
        return findByAttemptIdAndFormId(attemptId, formId).isPresent();
    }
}
