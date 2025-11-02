package com.pocopi.api.repositories;

import com.pocopi.api.models.test.UserTestQuestionLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO make projections
@Repository
public interface UserTestQuestionLogRepository extends JpaRepository<UserTestQuestionLogModel, Long> {
    @Query(
        value = """
            select utql.timestamp + 0 as start
            from user_test_question_log utql
                join user_test_attempt ta on ta.id = utql.attempt_id
                join test_question tq on tq.id = utql.question_id
                join test_phase tph on tph.id = tq.phase_id
                join test_protocol tp on tp.id = tph.protocol_id
                join config c on c.version = tp.config_version
            where c.version = :configVersion
                and ta.user_id = :userId
            order by utql.timestamp
            limit 1
            """,
        nativeQuery = true
    )
    Long findMostRecentlyStartTimeStamp(@Param("configVersion") int configVersion, @Param("userId") int userId);

    @Query(
        value = """
            select utql.timestamp + utql.duration as end
            from user_test_question_log utql
                join user_test_attempt ta on ta.id = utql.attempt_id
                join test_question tq on tq.id = utql.question_id
                join test_phase tph on tph.id = tq.phase_id
                join test_protocol tp on tp.id = tph.protocol_id
                join config c on c.version = tp.config_version
            where c.version = :configVersion
                and ta.user_id = :userId
            order by utql.timestamp desc
            limit 1
            """,
        nativeQuery = true
    )
    Long findMostRecentlyEndTimeStamp(@Param("configVersion") int configVersion, @Param("userId") int userId);

    @Query(
        value = """
            with timelog as (
                select
                    ta.user_id,
                    tq.phase_id,
                    utql.question_id,
                    min(utql.timestamp) as starttimestamp,
                    max(date_add(utql.timestamp, interval utql.duration second)) as endtimestamp
                from user_test_question_log utql
                    join user_test_attempt ta on ta.id = utql.attempt_id
                    join test_question tq on tq.id = utql.question_id
                    join test_phase tph on tph.id = tq.phase_id
                    join test_protocol tp on tp.id = tph.protocol_id
                    join config c on c.version = tp.config_version
                where c.version = :configVersion
                group by ta.user_id, tq.phase_id, utql.question_id
            ),
            last_non_hover as (
                select
                    ta.user_id,
                    o.question_id,
                    ol.option_id,
                    ol.type,
                    row_number() over (partition by ta.user_id, o.question_id order by ol.timestamp desc) as rn,
                    o.correct
                from user_test_option_log ol
                    join user_test_attempt ta on ta.id = ol.attempt_id
                    join test_option o on o.id = ol.option_id
                    join test_question q on q.id = o.question_id
                    join test_phase ph on ph.id = q.phase_id
                    join test_protocol pr on pr.id = ph.protocol_id
                    join config c on c.version = pr.config_version
                where c.version = :configVersion
                  and ol.type in ('select','deselect')
            ),
            final_selected as (
                select
                    user_id,
                    question_id,
                    option_id,
                    correct
                from last_non_hover
                where rn = 1
                    and type = 'select'
            ),
            correct_options as (
                select
                    question_id,
                    sum(correct) as correct_count
                from test_option
                group by question_id
            ),
            select_events as (
                select
                    ta.user_id,
                    o.question_id,
                    ol.option_id,
                    lag(ol.option_id) over (partition by ta.user_id, o.question_id order by ol.timestamp) as prev_option
                from user_test_option_log ol
                    join user_test_attempt ta on ta.id = ol.attempt_id
                    join test_option o on o.id = ol.option_id
                    join test_question q on q.id = o.question_id
                    join test_phase ph on ph.id = q.phase_id
                    join test_protocol pr on pr.id = ph.protocol_id
                    join config c on c.version = pr.config_version
                where c.version = :configVersion
                    and ol.type = 'select'
            ),
            option_changes as (
                select
                    user_id,
                    question_id,
                    sum(if(prev_option is not null and option_id <> prev_option, 1, 0)) as totaloptionchanges
                from select_events
                group by user_id, question_id
            ),
            hover_counts as (
                select
                    ta.user_id,
                    o.question_id,
                    count(*) as totaloptionhovers
                from user_test_option_log ol
                    join user_test_attempt ta on ta.id = ol.attempt_id
                    join test_option o on o.id = ol.option_id
                    join test_question q on q.id = o.question_id
                    join test_phase ph on ph.id = q.phase_id
                    join test_protocol pr on pr.id = ph.protocol_id
                    join config c on c.version = pr.config_version
                where c.version = :configVersion
                    and ol.type = 'hover'
                group by ta.user_id, o.question_id
            )
            select
                t.user_id,
                t.phase_id,
                t.question_id,
                unix_timestamp(t.starttimestamp)*1000                            as starttimestamp,
                unix_timestamp(t.endtimestamp)*1000                              as endtimestamp,
                if(coalesce(fc.correct_count, 0) = count(f.option_id)
                       and sum(f.correct) = coalesce(fc.correct_count, 0), 1, 0) as correct,
                if(count(f.option_id) = 0, 1, 0)                                 as skipped,
                coalesce(oc.totaloptionchanges,0)                                as totaloptionchanges,
                coalesce(hc.totaloptionhovers,0)                                 as totaloptionhovers
            from timelog t
                left join final_selected f on f.question_id = t.question_id and f.user_id = t.user_id
                left join correct_options fc on fc.question_id = t.question_id
                left join option_changes oc on oc.question_id = t.question_id and oc.user_id = t.user_id
                left join hover_counts hc on hc.question_id = t.question_id and hc.user_id = t.user_id
            group by t.user_id, t.phase_id, t.question_id, t.starttimestamp, t.endtimestamp,
                fc.correct_count, oc.totaloptionchanges, hc.totaloptionhovers
            order by t.user_id, t.phase_id, t.question_id
            """,
        nativeQuery = true
    )
    List<Object[]> findAllQuestionEvents(@Param("configVersion") int configVersion);

    @Query(
        value = """
            with timelog as (
                select
                    ta.user_id,
                    tq.phase_id,
                    utql.question_id,
                    min(utql.timestamp) as starttimestamp,
                    max(date_add(utql.timestamp, interval utql.duration second)) as endtimestamp
                from user_test_question_log utql
                    join user_test_attempt ta on ta.id = utql.attempt_id
                    join test_question tq on tq.id = utql.question_id
                    join test_phase tph on tph.id = tq.phase_id
                    join test_protocol tp on tp.id = tph.protocol_id
                    join config c on c.version = tp.config_version
                where c.version = :configVersion
                    and ta.user_id = :userId
                group by ta.user_id, tq.phase_id, utql.question_id
            ),
            last_non_hover as (
                select
                    ta.user_id,
                    o.question_id,
                    ol.option_id,
                    ol.type,
                    row_number() over (partition by ta.user_id, o.question_id order by ol.timestamp desc) as rn,
                    o.correct
                from user_test_option_log ol
                    join user_test_attempt ta on ta.id = ol.attempt_id
                    join test_option o on o.id = ol.option_id
                    join test_question q on q.id = o.question_id
                    join test_phase ph on ph.id = q.phase_id
                    join test_protocol pr on pr.id = ph.protocol_id
                    join config c on c.version = pr.config_version
                where c.version = :configVersion
                    and ta.user_id = :userId
                    and ol.type in ('select','deselect')
            ),
            final_selected as (
                select
                    user_id,
                    question_id,
                    option_id,
                    correct
                from last_non_hover
                where rn = 1
                    and type = 'select'
            ),
            correct_options as (
                select
                    question_id,
                    sum(correct) as correct_count
                from test_option
                group by question_id
            ),
            select_events as (
                select
                    ta.user_id,
                    o.question_id,
                    ol.option_id,
                    lag(ol.option_id) over (partition by ta.user_id, o.question_id order by ol.timestamp) as prev_option
                from user_test_option_log ol
                    join user_test_attempt ta on ta.id = ol.attempt_id
                    join test_option o on o.id = ol.option_id
                    join test_question q on q.id = o.question_id
                    join test_phase ph on ph.id = q.phase_id
                    join test_protocol pr on pr.id = ph.protocol_id
                    join config c on c.version = pr.config_version
                where c.version = :configVersion
                    and ta.user_id = :userId
                    and ol.type = 'select'
            ),
            option_changes as (
                select
                    user_id,
                    question_id,
                    sum(if(prev_option is not null and option_id <> prev_option, 1, 0)) as totaloptionchanges
                from select_events
                group by user_id, question_id
            ),
            hover_counts as (
                select
                    ta.user_id,
                    o.question_id,
                    count(*) as totaloptionhovers
                from user_test_option_log ol
                    join user_test_attempt ta on ta.id = ol.attempt_id
                    join test_option o on o.id = ol.option_id
                    join test_question q on q.id = o.question_id
                    join test_phase ph on ph.id = q.phase_id
                    join test_protocol pr on pr.id = ph.protocol_id
                    join config c on c.version = pr.config_version
                where c.version = :configVersion
                    and ta.user_id = :userId
                    and ol.type = 'hover'
                group by ta.user_id, o.question_id
            )
            select
                t.user_id,
                t.phase_id,
                t.question_id,
                unix_timestamp(t.starttimestamp)*1000                            as starttimestamp,
                unix_timestamp(t.endtimestamp)*1000                              as endtimestamp,
                if(coalesce(fc.correct_count, 0) = count(f.option_id)
                       and sum(f.correct) = coalesce(fc.correct_count, 0), 1, 0) as correct,
                if(count(f.option_id) = 0, 1, 0)                                 as skipped,
                coalesce(oc.totaloptionchanges,0)                                as totaloptionchanges,
                coalesce(hc.totaloptionhovers,0)                                 as totaloptionhovers
            from timelog t
                left join final_selected f on f.question_id = t.question_id and f.user_id = t.user_id
                left join correct_options fc on fc.question_id = t.question_id
                left join option_changes oc on oc.question_id = t.question_id and oc.user_id = t.user_id
                left join hover_counts hc on hc.question_id = t.question_id and hc.user_id = t.user_id
            group by t.user_id, t.phase_id, t.question_id, t.starttimestamp, t.endtimestamp,
                fc.correct_count, oc.totaloptionchanges, hc.totaloptionhovers
            order by t.user_id, t.phase_id, t.question_id
            """,
        nativeQuery = true
    )
    List<Object[]> findAllQuestionEventsInfoByUserId(
        @Param("configVersion") int configVersion,
        @Param("userId") int userId
    );
}
