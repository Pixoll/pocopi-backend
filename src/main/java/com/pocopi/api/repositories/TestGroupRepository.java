package com.pocopi.api.repositories;

import com.pocopi.api.models.test.TestGroupModel;
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
            tp.allow_previous_phase as allowPreviousPhase,
            tp.allow_previous_question as allowPreviousQuestion,
            tp.allow_skip_question as allowSkipQuestion,
            tph.id as phase_id,
            tq.id as question_id,
            tq.text as question_text,
            tq.order as question_order,
            tq.image_id as question_image_id,
            topt.id as optionId,
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
