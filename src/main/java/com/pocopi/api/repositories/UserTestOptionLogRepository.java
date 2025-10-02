package com.pocopi.api.repositories;

import com.pocopi.api.models.UserTestOptionLogModel;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
