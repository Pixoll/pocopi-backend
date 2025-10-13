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

    @Query(value = """
        with question_log as (
            select o.question_id,
                   ol.timestamp,
                   ol.type,
                   o.correct,
                   row_number() over (partition by o.question_id order by ol.timestamp desc) as first
            from user_test_option_log ol
                     inner join test_option o on o.id = ol.option_id
                     inner join test_question q on q.id = o.question_id
                     inner join test_phase ph on ph.id = q.phase_id
                     inner join test_protocol pr on pr.id = ph.protocol_id
                     inner join config c on c.version = pr.config_version
            where ol.user_id = :userId
              and c.version = :configVersion
              and ol.type in ('select', 'deselect')
        )
        select question_id,
               timestamp,
               type,
               correct
        from question_log
        where first = 1
        """, nativeQuery = true)
    List<Object[]> findAllLastOptionsByUserId(@Param("userId") int userId,
                                              @Param("configVersion") int configVersion);

    @Query(value = """
            SELECT
                tq.id AS question_id,
                utol.type,
                utol.option_id,
                UNIX_TIMESTAMP(utol.timestamp) * 1000 AS timestamp,
                utol.user_id
            FROM user_test_option_log utol
                 JOIN test_option to_opt ON to_opt.id = utol.option_id
                 JOIN test_question tq ON tq.id = to_opt.question_id
                 JOIN test_phase tph ON tph.id = tq.phase_id
                 JOIN test_protocol tp ON tp.id = tph.protocol_id
                 JOIN config c ON c.version = tp.config_version
            WHERE c.version = :configVersion
            ORDER BY tq.id, utol.user_id, utol.timestamp
""", nativeQuery = true)
    List<Object[]> findAllEventByLastConfig(@Param("configVersion") int configVersion);

    @Query(value = """
            SELECT
                tq.id AS question_id,
                utol.type,
                utol.option_id,
                UNIX_TIMESTAMP(utol.timestamp) * 1000 AS timestamp,
                utol.user_id
            FROM user_test_option_log utol
                 JOIN test_option to_opt ON to_opt.id = utol.option_id
                 JOIN test_question tq ON tq.id = to_opt.question_id
                 JOIN test_phase tph ON tph.id = tq.phase_id
                 JOIN test_protocol tp ON tp.id = tph.protocol_id
                 JOIN config c ON c.version = tp.config_version
            WHERE c.version = :configVersion
                  AND utol.user_id = :userId
            ORDER BY tq.id, utol.user_id, utol.timestamp
""", nativeQuery = true)
    List<Object[]> findAllEventByUserIdAndConfigVersion(@Param("configVersion") int configVersion,  @Param("userId") int userId);

    @Modifying
    @Query(value = """
        INSERT INTO user_test_option_log (user_id, option_id, type, timestamp)
        VALUES (:userId, :optionId, :type, NOW(3))
        """, nativeQuery = true)
    void insertUserTestOptionLog(
        @Param("userId") int userId,
        @Param("optionId") int optionId,
        @Param("type") String type
    );
}
