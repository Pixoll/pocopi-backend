package com.pocopi.api.repositories;

import com.pocopi.api.models.test.TestGroupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TestGroupRepository extends JpaRepository<TestGroupModel, Integer> {
    @Query(
        value = """
            select
                tg.id as group_id,
                tg.config_version,
                tg.label as group_label,
                tg.probability,
                tg.greeting,
                tp.id as protocol_id,
                tp.label as protocol_label,
                tp.allow_previous_phase as allowpreviousphase,
                tp.allow_previous_question as allowpreviousquestion,
                tp.allow_skip_question as allowskipquestion,
                tph.id as phase_id,
                tq.id as question_id,
                tq.text as question_text,
                tq.order as question_order,
                tq.image_id as question_image_id,
                topt.id as optionid,
                topt.text as option_text,
                topt.image_id as option_image_id,
                topt.correct
            from test_group tg
                 left join test_protocol tp on tg.id = tp.group_id
                 left join test_phase tph on tp.id = tph.protocol_id
                 left join test_question tq on tph.id = tq.phase_id
                 left join test_option topt on tq.id = topt.question_id
            where tg.config_version = :configVersion
            """,
        nativeQuery = true
    )
    List<TestGroupData> findAllGroupsDataByConfigVersion(int configVersion);

    List<TestGroupModel> findAllByConfig_Version(int version);
}
