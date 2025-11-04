package com.pocopi.api.repositories;

import com.pocopi.api.models.test.UserTestQuestionLogModel;
import com.pocopi.api.repositories.projections.QuestionEventProjection;
import com.pocopi.api.repositories.projections.QuestionEventWithUserIdProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO make projections
@Repository
public interface UserTestQuestionLogRepository extends JpaRepository<UserTestQuestionLogModel, Long> {
    @NativeQuery(
        """
            with
                timelog as (
                    select ta.user_id,
                           ql.question_id,
                           json_arrayagg(
                               json_object(
                                   'start', cast(unix_timestamp(ql.timestamp) * 1000 as unsigned),
                                   'end', cast(unix_timestamp(ql.timestamp) * 1000 + ql.duration as unsigned)
                               )
                           ) as timestamps
                        from user_test_question_log     ql
                            left join user_test_attempt ta on ta.id = ql.attempt_id
                            left join test_question     tq on tq.id = ql.question_id
                            left join test_group        tg on tg.id = ta.group_id
                        where tg.config_version = :configVersion
                        group by ta.user_id, ql.question_id
                    ),
                last_non_hover as (
                    select ta.user_id,
                           o.question_id,
                           ol.option_id,
                           ol.type,
                           o.correct,
                           row_number() over (partition by o.question_id order by ol.timestamp desc) as rn
                        from user_test_option_log       ol
                            left join user_test_attempt ta on ta.id = ol.attempt_id
                            left join test_option       o on o.id = ol.option_id
                            left join test_group        tg on tg.id = ta.group_id
                        where tg.config_version = :configVersion
                          and ol.type in ('select', 'deselect')
                    ),
                last_selected as (
                    select user_id,
                           question_id,
                           correct
                        from last_non_hover
                        where rn = 1
                          and type = 'select'
                    ),
                option_changes as (
                    select ta.user_id,
                           o.question_id,
                           greatest(0, count(*) - 1) as total_option_changes
                        from user_test_option_log       ol
                            left join user_test_attempt ta on ta.id = ol.attempt_id
                            left join test_option       o on o.id = ol.option_id
                            left join test_group        tg on tg.id = ta.group_id
                        where tg.config_version = :configVersion
                          and ol.type = 'select'
                        group by ta.user_id, o.question_id
                    ),
                hover_counts as (
                    select ta.user_id,
                           o.question_id,
                           count(*) as total_option_hovers
                        from user_test_option_log       ol
                            left join user_test_attempt ta on ta.id = ol.attempt_id
                            left join test_option       o on o.id = ol.option_id
                            left join test_group        tg on tg.id = ta.group_id
                        where tg.config_version = :configVersion
                          and ol.type = 'hover'
                        group by ta.user_id, o.question_id
                    )
            select t.user_id,
                   t.question_id,
                   t.timestamps                         as timestamps_json,
                   coalesce(ls.correct, false)          as correct,
                   ls.correct is null                   as skipped,
                   coalesce(oc.total_option_changes, 0) as total_option_changes,
                   coalesce(hc.total_option_hovers, 0)  as total_option_hovers
                from timelog                 t
                    left join last_selected  ls on ls.question_id = t.question_id and ls.user_id = t.user_id
                    left join option_changes oc on oc.question_id = t.question_id and oc.user_id = t.user_id
                    left join hover_counts   hc on hc.question_id = t.question_id and hc.user_id = t.user_id
                group by t.user_id, t.question_id, t.timestamps, ls.correct, oc.total_option_changes,
                         hc.total_option_hovers
                order by t.user_id, t.question_id
            """
    )
    List<QuestionEventWithUserIdProjection> findAllQuestionEvents(int configVersion);

    @NativeQuery(
        """
            with
                timelog as (
                    select ql.question_id,
                           json_arrayagg(
                               json_object(
                                   'start', cast(unix_timestamp(ql.timestamp) * 1000 as unsigned),
                                   'end', cast(unix_timestamp(ql.timestamp) * 1000 + ql.duration as unsigned)
                               )
                           ) as timestamps
                        from user_test_question_log     ql
                            left join user_test_attempt ta on ta.id = ql.attempt_id
                            left join test_question     tq on tq.id = ql.question_id
                            left join test_group        tg on tg.id = ta.group_id
                        where tg.config_version = :configVersion
                          and ta.user_id = :userId
                        group by ql.question_id
                    ),
                last_non_hover as (
                    select o.question_id,
                           ol.option_id,
                           ol.type,
                           o.correct,
                           row_number() over (partition by o.question_id order by ol.timestamp desc) as rn
                        from user_test_option_log       ol
                            left join user_test_attempt ta on ta.id = ol.attempt_id
                            left join test_option       o on o.id = ol.option_id
                            left join test_group        tg on tg.id = ta.group_id
                        where tg.config_version = :configVersion
                          and ta.user_id = :userId
                          and ol.type in ('select', 'deselect')
                    ),
                last_selected as (
                    select question_id,
                           correct
                        from last_non_hover
                        where rn = 1
                          and type = 'select'
                    ),
                option_changes as (
                    select o.question_id,
                           greatest(0, count(*) - 1) as total_option_changes
                        from user_test_option_log       ol
                            left join user_test_attempt ta on ta.id = ol.attempt_id
                            left join test_option       o on o.id = ol.option_id
                            left join test_group        tg on tg.id = ta.group_id
                        where tg.config_version = :configVersion
                          and ta.user_id = :userId
                          and ol.type = 'select'
                        group by o.question_id
                    ),
                hover_counts as (
                    select o.question_id,
                           count(*) as total_option_hovers
                        from user_test_option_log       ol
                            left join user_test_attempt ta on ta.id = ol.attempt_id
                            left join test_option       o on o.id = ol.option_id
                            left join test_group        tg on tg.id = ta.group_id
                        where tg.config_version = :configVersion
                          and ta.user_id = :userId
                          and ol.type = 'hover'
                        group by o.question_id
                    )
            select t.question_id,
                   t.timestamps                         as timestamps_json,
                   coalesce(ls.correct, false)          as correct,
                   ls.correct is null                   as skipped,
                   coalesce(oc.total_option_changes, 0) as total_option_changes,
                   coalesce(hc.total_option_hovers, 0)  as total_option_hovers
                from timelog                 t
                    left join last_selected  ls on ls.question_id = t.question_id
                    left join option_changes oc on oc.question_id = t.question_id
                    left join hover_counts   hc on hc.question_id = t.question_id
                group by t.question_id, t.timestamps, ls.correct, oc.total_option_changes, hc.total_option_hovers
                order by t.question_id
            """
    )
    List<QuestionEventProjection> findAllQuestionEventsByUserId(int configVersion, int userId);
}
