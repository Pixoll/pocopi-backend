package com.pocopi.api.repositories;

import com.pocopi.api.models.form.UserFormAnswerModel;
import com.pocopi.api.repositories.projections.UserFormAnswerProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFormAnswerRepository extends JpaRepository<UserFormAnswerModel, Integer> {
    @NativeQuery(
        """
            select fa.id,
                   f.config_version,
                   fs.form_id,
                   f.type as form_type,
                   fa.question_id,
                   fa.option_id,
                   fa.value,
                   fa.answer
                from user_form_answer               fa
                    inner join user_form_submission fs on fs.id = fa.form_sub_id
                    inner join user_test_attempt    ta on ta.id = fs.attempt_id
                    inner join form                 f on f.id = fs.form_id
                where ta.user_id = :userId
                  and ta.end is not null
            """
    )
    List<UserFormAnswerProjection> findAllByUserId(int userId);

    @NativeQuery(
        """
            select fa.id,
                   f.config_version,
                   fs.form_id,
                   f.type as form_type,
                   fa.question_id,
                   fa.option_id,
                   fa.value,
                   fa.answer
                from user_form_answer               fa
                    inner join user_form_submission fs on fs.id = fa.form_sub_id
                    inner join user_test_attempt    ta on ta.id = fs.attempt_id
                    inner join form                 f on f.id = fs.form_id
                where ta.id = :attemptId
            """
    )
    List<UserFormAnswerProjection> findAllByAttemptId(long attemptId);

    boolean existsByOptionId(int optionId);

    boolean existsByQuestionId(int questionId);
}
