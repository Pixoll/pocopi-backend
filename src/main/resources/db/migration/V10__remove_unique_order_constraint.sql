alter table form_question
    drop constraint form_id,
    add index (form_id);

alter table form_question_option
    drop constraint form_question_id,
    add index (form_question_id);

alter table home_faq
    drop constraint config_version,
    add index (config_version);

alter table home_info_card
    drop constraint config_version,
    add index (config_version);

alter table test_phase
    drop constraint group_id,
    add index (group_id);

alter table test_question
    drop constraint phase_id,
    add index (phase_id);

alter table test_option
    drop constraint question_id,
    add index (question_id);
