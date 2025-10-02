package com.pocopi.api.repositories;

import com.pocopi.api.models.TestGroupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TestGroupRepository extends JpaRepository<TestGroupModel, Integer> {
    @Query(value = """
        SELECT
            tg.id as group_id,
            tg.config_version,
            tg.label as group_label,
            tg.probability,
            tg.greeting,
            tph.order as phase_order,
            tq.order as question_order,
            tq.image_id as question_image_id,
            topt.order as option_order,
            topt.text as option_text,
            topt.image_id as option_image_id,
            topt.correct
        FROM test_group tg
                 LEFT JOIN test_protocol tp ON tg.id = tp.group_id
                 LEFT JOIN test_phase tph ON tp.id = tph.protocol_id
                 LEFT JOIN test_question tq ON tph.id = tq.phase_id
                 LEFT JOIN test_option topt ON tq.id = topt.question_id
        WHERE tg.config_version = :configVersion
        """,nativeQuery = true)
    List<TestGroupData> findAllGroupsDataByConfigVersion(int configVersion);
}
