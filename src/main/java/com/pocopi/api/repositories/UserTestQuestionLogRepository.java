package com.pocopi.api.repositories;

import com.pocopi.api.models.UserTestQuestionLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
}
