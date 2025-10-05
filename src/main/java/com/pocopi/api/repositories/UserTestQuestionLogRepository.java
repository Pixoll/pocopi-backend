package com.pocopi.api.repositories;

import com.pocopi.api.models.UserTestQuestionLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserTestQuestionLogRepository extends JpaRepository<UserTestQuestionLogModel, Long> {
    @Query(value = """
        SELECT utql.timestamp + 0 as start
        FROM user_test_question_log utql
                 JOIN test_question tq ON tq.id = utql.question_id
                 JOIN test_phase tph ON tph.id = tq.phase_id
                 JOIN test_protocol tp ON tp.id = tph.protocol_id
                 JOIN config c ON c.version = tp.config_version
        WHERE c.version = :configVersion
          AND utql.user_id = :userId
        ORDER BY utql.timestamp
        LIMIT 1
        """,
        nativeQuery = true)
    Long  findMostRecentlyStartTimeStamp(@Param("configVersion")int configVersion, @Param("userId") int userId);

    @Query(value = """
        SELECT utql.timestamp + utql.duration as end
        FROM user_test_question_log utql
                 JOIN test_question tq ON tq.id = utql.question_id
                 JOIN test_phase tph ON tph.id = tq.phase_id
                 JOIN test_protocol tp ON tp.id = tph.protocol_id
                 JOIN config c ON c.version = tp.config_version
        WHERE c.version = :configVersion
          AND utql.user_id = :userId
        ORDER BY utql.timestamp DESC
        LIMIT 1
        """,
        nativeQuery = true)
    Long  findMostRecentlyEndTimeStamp(@Param("configVersion")int configVersion, @Param("userId") int userId);

    @Query(value = """
        WITH timelog AS (
            SELECT
                utql.user_id,
                tq.phase_id,
                utql.question_id,
                MIN(utql.timestamp) AS startTimestamp,
                MAX(DATE_ADD(utql.timestamp, INTERVAL utql.duration SECOND)) AS endTimestamp
            FROM user_test_question_log utql
                     JOIN test_question tq ON tq.id = utql.question_id
                     JOIN test_phase tph ON tph.id = tq.phase_id
                     JOIN test_protocol tp ON tp.id = tph.protocol_id
                     JOIN config c ON c.version = tp.config_version
            WHERE c.version = :configVersion
            GROUP BY utql.user_id, tq.phase_id, utql.question_id
        ),
        last_non_hover AS (
            SELECT
                ol.user_id,
                o.question_id,
                ol.option_id,
                ol.type,
                ROW_NUMBER() OVER (PARTITION BY ol.user_id, o.question_id ORDER BY ol.timestamp DESC) AS rn,
                o.correct
            FROM user_test_option_log ol
                     JOIN test_option o ON o.id = ol.option_id
                     JOIN test_question q ON q.id = o.question_id
                     JOIN test_phase ph ON ph.id = q.phase_id
                     JOIN test_protocol pr ON pr.id = ph.protocol_id
                     JOIN config c ON c.version = pr.config_version
            WHERE c.version = :configVersion
              AND ol.type IN ('select','deselect')
        ),
        final_selected AS (
            SELECT
                user_id,
                question_id,
                option_id,
                correct
            FROM last_non_hover
            WHERE rn = 1
              AND type = 'select'
        ),
        correct_options AS (
            SELECT
                question_id,
                SUM(correct) AS correct_count
            FROM test_option
            GROUP BY question_id
        ),
        select_events AS (
            SELECT
                ol.user_id,
                o.question_id,
                ol.option_id,
                LAG(ol.option_id) OVER (PARTITION BY ol.user_id, o.question_id ORDER BY ol.timestamp) AS prev_option
            FROM user_test_option_log ol
                     JOIN test_option o ON o.id = ol.option_id
                     JOIN test_question q ON q.id = o.question_id
                     JOIN test_phase ph ON ph.id = q.phase_id
                     JOIN test_protocol pr ON pr.id = ph.protocol_id
                     JOIN config c ON c.version = pr.config_version
            WHERE c.version = :configVersion
              AND ol.type = 'select'
        ),
        option_changes AS (
            SELECT
                user_id,
                question_id,
                SUM(IF(prev_option IS NOT NULL AND option_id <> prev_option, 1, 0)) AS totalOptionChanges
            FROM select_events
            GROUP BY user_id, question_id
        ),
        hover_counts AS (
            SELECT
                ol.user_id,
                o.question_id,
                COUNT(*) AS totalOptionHovers
            FROM user_test_option_log ol
                     JOIN test_option o ON o.id = ol.option_id
                     JOIN test_question q ON q.id = o.question_id
                     JOIN test_phase ph ON ph.id = q.phase_id
                     JOIN test_protocol pr ON pr.id = ph.protocol_id
                     JOIN config c ON c.version = pr.config_version
            WHERE c.version = :configVersion
              AND ol.type = 'hover'
            GROUP BY ol.user_id, o.question_id
        )
        SELECT
            t.user_id,
            t.phase_id,
            t.question_id,
            UNIX_TIMESTAMP(t.startTimestamp)*1000                            AS startTimestamp,
            UNIX_TIMESTAMP(t.endTimestamp)*1000                              AS endTimestamp,
            IF(COALESCE(fc.correct_count, 0) = COUNT(f.option_id)
                   AND SUM(f.correct) = COALESCE(fc.correct_count, 0), 1, 0) AS correct,
            IF(COUNT(f.option_id) = 0, 1, 0)                                 AS skipped,
            COALESCE(oc.totalOptionChanges,0)                                AS totalOptionChanges,
            COALESCE(hc.totalOptionHovers,0)                                 AS totalOptionHovers
        FROM timelog t
                 LEFT JOIN final_selected f ON f.question_id = t.question_id AND f.user_id = t.user_id
                 LEFT JOIN correct_options fc ON fc.question_id = t.question_id
                 LEFT JOIN option_changes oc ON oc.question_id = t.question_id AND oc.user_id = t.user_id
                 LEFT JOIN hover_counts hc ON hc.question_id = t.question_id AND hc.user_id = t.user_id
        GROUP BY t.user_id, t.phase_id, t.question_id, t.startTimestamp, t.endTimestamp,
                 fc.correct_count, oc.totalOptionChanges, hc.totalOptionHovers
        ORDER BY t.user_id, t.phase_id, t.question_id
        """,
        nativeQuery = true)
    List<Object[]> findAllQuestionEvents(@Param("configVersion") int configVersion);


    @Query(value = """
        WITH timelog AS (
            SELECT
                utql.user_id,
                tq.phase_id,
                utql.question_id,
                MIN(utql.timestamp) AS startTimestamp,
                MAX(DATE_ADD(utql.timestamp, INTERVAL utql.duration SECOND)) AS endTimestamp
            FROM user_test_question_log utql
                     JOIN test_question tq ON tq.id = utql.question_id
                     JOIN test_phase tph ON tph.id = tq.phase_id
                     JOIN test_protocol tp ON tp.id = tph.protocol_id
                     JOIN config c ON c.version = tp.config_version
            WHERE c.version = :configVersion
                  AND ol.user_id = :userId
            GROUP BY utql.user_id, tq.phase_id, utql.question_id
        ),
        last_non_hover AS (
            SELECT
                ol.user_id,
                o.question_id,
                ol.option_id,
                ol.type,
                ROW_NUMBER() OVER (PARTITION BY ol.user_id, o.question_id ORDER BY ol.timestamp DESC) AS rn,
                o.correct
            FROM user_test_option_log ol
                     JOIN test_option o ON o.id = ol.option_id
                     JOIN test_question q ON q.id = o.question_id
                     JOIN test_phase ph ON ph.id = q.phase_id
                     JOIN test_protocol pr ON pr.id = ph.protocol_id
                     JOIN config c ON c.version = pr.config_version
            WHERE c.version = :configVersion
                AND ol.user_id = :userId
                AND ol.type IN ('select','deselect')
        ),
        final_selected AS (
            SELECT
                user_id,
                question_id,
                option_id,
                correct
            FROM last_non_hover
            WHERE rn = 1
              AND type = 'select'
        ),
        correct_options AS (
            SELECT
                question_id,
                SUM(correct) AS correct_count
            FROM test_option
            GROUP BY question_id
        ),
        select_events AS (
            SELECT
                ol.user_id,
                o.question_id,
                ol.option_id,
                LAG(ol.option_id) OVER (PARTITION BY ol.user_id, o.question_id ORDER BY ol.timestamp) AS prev_option
            FROM user_test_option_log ol
                     JOIN test_option o ON o.id = ol.option_id
                     JOIN test_question q ON q.id = o.question_id
                     JOIN test_phase ph ON ph.id = q.phase_id
                     JOIN test_protocol pr ON pr.id = ph.protocol_id
                     JOIN config c ON c.version = pr.config_version
            WHERE c.version = :configVersion
                AND ol.user_id = :userId
                AND ol.type = 'select'
        ),
        option_changes AS (
            SELECT
                user_id,
                question_id,
                SUM(IF(prev_option IS NOT NULL AND option_id <> prev_option, 1, 0)) AS totalOptionChanges
            FROM select_events
            GROUP BY user_id, question_id
        ),
        hover_counts AS (
            SELECT
                ol.user_id,
                o.question_id,
                COUNT(*) AS totalOptionHovers
            FROM user_test_option_log ol
                     JOIN test_option o ON o.id = ol.option_id
                     JOIN test_question q ON q.id = o.question_id
                     JOIN test_phase ph ON ph.id = q.phase_id
                     JOIN test_protocol pr ON pr.id = ph.protocol_id
                     JOIN config c ON c.version = pr.config_version
            WHERE c.version = :configVersion
              AND ol.user_id = :userId
              AND ol.type = 'hover'
            GROUP BY ol.user_id, o.question_id
        )
        SELECT
            t.user_id,
            t.phase_id,
            t.question_id,
            UNIX_TIMESTAMP(t.startTimestamp)*1000                            AS startTimestamp,
            UNIX_TIMESTAMP(t.endTimestamp)*1000                              AS endTimestamp,
            IF(COALESCE(fc.correct_count, 0) = COUNT(f.option_id)
                   AND SUM(f.correct) = COALESCE(fc.correct_count, 0), 1, 0) AS correct,
            IF(COUNT(f.option_id) = 0, 1, 0)                                 AS skipped,
            COALESCE(oc.totalOptionChanges,0)                                AS totalOptionChanges,
            COALESCE(hc.totalOptionHovers,0)                                 AS totalOptionHovers
        FROM timelog t
                 LEFT JOIN final_selected f ON f.question_id = t.question_id AND f.user_id = t.user_id
                 LEFT JOIN correct_options fc ON fc.question_id = t.question_id
                 LEFT JOIN option_changes oc ON oc.question_id = t.question_id AND oc.user_id = t.user_id
                 LEFT JOIN hover_counts hc ON hc.question_id = t.question_id AND hc.user_id = t.user_id
        GROUP BY t.user_id, t.phase_id, t.question_id, t.startTimestamp, t.endTimestamp,
                 fc.correct_count, oc.totalOptionChanges, hc.totalOptionHovers
        ORDER BY t.user_id, t.phase_id, t.question_id
        """,
        nativeQuery = true)
    List<Object[]> findAllQuestionEventsInfoByUserId(@Param("configVersion") int configVersion, @Param("userId") int userId);
}
