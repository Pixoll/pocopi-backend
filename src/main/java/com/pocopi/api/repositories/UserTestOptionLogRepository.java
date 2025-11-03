package com.pocopi.api.repositories;

import com.pocopi.api.models.test.UserTestOptionLogModel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTestOptionLogRepository extends JpaRepository<UserTestOptionLogModel, Integer> {
    @Query(
        value = """
            with question_log as (
                select
                    o.question_id,
                    ol.timestamp,
                    ol.type,
                    o.correct,
                    row_number() over (partition by o.question_id order by ol.timestamp desc) as first
                from user_test_option_log ol
                    inner join user_test_attempt ta on ta.id = ol.attempt_id
                    inner join test_option o on o.id = ol.option_id
                    inner join test_question q on q.id = o.question_id
                    inner join test_phase ph on ph.id = q.phase_id
                    inner join test_group tg on tg.id = ph.group_id
                    inner join config c on c.version = tg.config_version
                where ta.user_id = :userId
                    and c.version = :configVersion
                    and ol.type in ('select', 'deselect')
            )
            select
                question_id,
                timestamp,
                type,
                correct
            from question_log
            where first = 1
            """, nativeQuery = true
    )
    List<Object[]> findAllLastOptionsByUserId(@Param("userId") int userId, @Param("configVersion") int configVersion);

    @Query(
        value = """
            select
                tq.id as question_id,
                utol.type,
                utol.option_id,
                unix_timestamp(utol.timestamp) * 1000 as timestamp,
                ta.user_id
            from user_test_option_log utol
                join user_test_attempt ta on ta.id = utol.attempt_id
                join test_option to_opt on to_opt.id = utol.option_id
                join test_question tq on tq.id = to_opt.question_id
                join test_phase tph on tph.id = tq.phase_id
                join test_group tg on tg.id = tph.group_id
                join config c on c.version = tg.config_version
            where c.version = :configVersion
            order by tq.id, ta.user_id, utol.timestamp
            """,
        nativeQuery = true
    )
    List<Object[]> findAllEventByLastConfig(@Param("configVersion") int configVersion);

    @Query(
        value = """
            select
                tq.id as question_id,
                utol.type,
                utol.option_id,
                unix_timestamp(utol.timestamp) * 1000 as timestamp,
                ta.user_id
            from user_test_option_log utol
                join user_test_attempt ta on ta.id = utol.attempt_id
                join test_option to_opt on to_opt.id = utol.option_id
                join test_question tq on tq.id = to_opt.question_id
                join test_phase tph on tph.id = tq.phase_id
                join test_group tg on tg.id = tph.group_id
                join config c on c.version = tg.config_version
            where c.version = :configVersion
            and ta.user_id = :userId
            order by tq.id, ta.user_id, utol.timestamp
            """,
        nativeQuery = true
    )
    List<Object[]> findAllEventByUserIdAndConfigVersion(
        @Param("configVersion") int configVersion,
        @Param("userId") int userId
    );

    @Modifying
    @Query(
        value = """
            insert into user_test_option_log (user_id, option_id, type, timestamp)
            values (:userId, :optionId, :type, now(3))
            """,
        nativeQuery = true
    )
    void insertUserTestOptionLog(
        @Param("userId") int userId,
        @Param("optionId") int optionId,
        @Param("type") String type
    );
}
