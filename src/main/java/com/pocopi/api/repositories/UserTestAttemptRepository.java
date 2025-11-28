package com.pocopi.api.repositories;

import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.repositories.projections.FormsCompletionStatusProjection;
import com.pocopi.api.repositories.projections.TestAnswerProjection;
import com.pocopi.api.repositories.projections.UserTestAttemptWithGroupProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import java.util.List;
import java.util.Optional;

public interface UserTestAttemptRepository extends JpaRepository<UserTestAttemptModel, Long> {
    @NativeQuery(
        """
            select ta.*
                from user_test_attempt    ta
                    inner join test_group g on g.id = ta.group_id
                where g.config_version = :configVersion
                  and ta.user_id = :userId
                  and ta.end is null
            """
    )
    Optional<UserTestAttemptModel> findUnfinishedAttempt(int configVersion, int userId);

    @NativeQuery(
        """
            select ta.*
                from user_test_attempt    ta
                    inner join test_group g on g.id = ta.group_id
                where g.config_version = :configVersion
                  and ta.user_id = :userId
                  and ta.end is not null
                order by ta.start
                limit 1
            """
    )
    Optional<UserTestAttemptModel> findLatestFinishedAttempt(int configVersion, int userId);

    @NativeQuery(
        """
            select ta.id,
                   g.config_version as config_version,
                   g.label          as `group`,
                   cast(unix_timestamp(ta.start) * 1000 as unsigned) as start,
                   cast(unix_timestamp(ta.end) * 1000 as unsigned) as end
                from user_test_attempt    ta
                    inner join test_group g on g.id = ta.group_id
                  and ta.end is not null
                order by ta.start
            """
    )
    List<UserTestAttemptWithGroupProjection> findFinishedAttempts();

    @NativeQuery(
        """
            select ta.id,
                   g.config_version as config_version,
                   g.label          as `group`,
                   cast(unix_timestamp(ta.start) * 1000 as unsigned) as start,
                   cast(unix_timestamp(ta.end) * 1000 as unsigned) as end
                from user_test_attempt    ta
                    inner join test_group g on g.id = ta.group_id
                  and ta.end is not null
                where ta.user_id = :userId
                order by ta.start
            """
    )
    List<UserTestAttemptWithGroupProjection> findFinishedAttemptsByUserId(int userId);

    @NativeQuery(
        """
            select coalesce(max(if(f.type = 'pre', 1, 0)) = 1, false)  as completed_pre_test_form,
                  coalesce(max(if(f.type = 'post', 1, 0)) = 1, false) as completed_post_test_form
               from user_test_attempt              ta
                   inner join user_form_submission fs on ta.id = fs.attempt_id
                   inner join form                 f on fs.form_id = f.id
               where ta.id = :attemptId
            """
    )
    FormsCompletionStatusProjection getFormsCompletionStatus(long attemptId);

    @NativeQuery(
        """
            with
                last_non_hover as (
                    select o.question_id,
                           ol.option_id,
                           ol.type,
                           row_number() over (partition by o.question_id order by ol.timestamp desc) as rn
                        from user_test_option_log       ol
                            left join user_test_attempt ta on ta.id = ol.attempt_id
                            left join test_option       o on o.id = ol.option_id
                            left join test_group        tg on tg.id = ta.group_id
                        where ta.id = :attemptId
                          and ol.type in ('select', 'deselect')
                        order by ol.timestamp
                    )
            select question_id, option_id
                from last_non_hover
                where rn = 1
                  and type = 'select'
            """
    )
    List<TestAnswerProjection> getTestAnswers(long attemptId);

    default boolean hasUnfinishedAttempt(int configVersion, int userId) {
        return findUnfinishedAttempt(configVersion, userId).isPresent();
    }

    boolean existsByGroupId(int groupId);
}
