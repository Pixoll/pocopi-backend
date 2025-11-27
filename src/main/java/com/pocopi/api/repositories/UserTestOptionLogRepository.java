package com.pocopi.api.repositories;

import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.UserTestOptionLogModel;
import com.pocopi.api.repositories.projections.LastSelectedOptionProjection;
import com.pocopi.api.repositories.projections.LastSelectedOptionWithAttemptProjection;
import com.pocopi.api.repositories.projections.OptionEventProjection;
import com.pocopi.api.repositories.projections.OptionEventWithUserIdProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTestOptionLogRepository extends JpaRepository<UserTestOptionLogModel, Integer> {
    @NativeQuery(
        """
            with
                question_log as (
                    select o.question_id,
                           ol.type,
                           o.correct,
                           row_number() over (partition by o.question_id order by ol.timestamp desc) as first
                        from user_test_option_log        ol
                            inner join test_option       o on o.id = ol.option_id
                        where ol.attempt_id = :attemptId
                          and ol.type in ('select', 'deselect')
                    )
            select question_id,
                   correct
                from question_log
                where first = 1 and type = 'select'
            """
    )
    List<LastSelectedOptionProjection> findLastSelectedOptionsByAttemptId(long attemptId);

    @NativeQuery(
        """
            with
                question_log as (
                    select ol.attempt_id,
                           ta.user_id,
                           o.question_id,
                           ol.type,
                           o.correct,
                           row_number() over (partition by o.question_id order by ol.timestamp desc) as first
                        from user_test_option_log        ol
                            inner join user_test_attempt ta on ta.id = ol.attempt_id
                            inner join test_option       o on o.id = ol.option_id
                        where ol.attempt_id in :attemptIds
                          and ol.type in ('select', 'deselect')
                    )
            select attempt_id,
                   user_id,
                   question_id,
                   correct
                from question_log
                where first = 1
                  and type = 'select'
            
            """
    )
    List<LastSelectedOptionWithAttemptProjection> findLastSelectedOptionsByAttemptIds(List<Long> attemptIds);

    @NativeQuery(
        """
            select ta.user_id,
                   o.question_id,
                   ol.type,
                   ol.option_id,
                   cast(unix_timestamp(ol.timestamp) * 1000 as unsigned) as timestamp
                from user_test_option_log  ol
                    join user_test_attempt ta on ta.id = ol.attempt_id
                    join test_option       o on o.id = ol.option_id
                    join test_group        tg on tg.id = ta.group_id
                where tg.config_version = :configVersion
                order by ta.user_id, o.question_id, ol.timestamp
            """
    )
    List<OptionEventWithUserIdProjection> findAllOptionEvents(int configVersion);

    @NativeQuery(
        """
            select o.question_id,
                   ol.type,
                   ol.option_id,
                   cast(unix_timestamp(ol.timestamp) * 1000 as unsigned) as timestamp
                from user_test_option_log  ol
                    join user_test_attempt ta on ta.id = ol.attempt_id
                    join test_option       o on o.id = ol.option_id
                    join test_group        tg on tg.id = ta.group_id
                where tg.config_version = :configVersion
                  and ta.user_id = :userId
                order by ta.user_id, o.question_id, ol.timestamp
            """
    )
    List<OptionEventProjection> findAllOptionEventsByUserId(int configVersion, int userId);

    boolean existsByOptionId(int optionId);

    int option(TestOptionModel option);

    boolean existsByOptionQuestionPhaseId(int optionQuestionPhaseId);
}
