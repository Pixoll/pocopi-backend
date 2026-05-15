insert into home_info_card (config_version, `order`, title, description, color)
    values (1, 0, 'Card 1', 'Description 1', 0xFF8080),
           (1, 1, 'Card 2', 'Description 2', 0x80FF80),
           (1, 2, 'Card 3', 'Description 3', 0x8080FF);

insert into home_faq (config_version, `order`, question, answer)
    values (1, 0, 'What is this?', 'An **answer**.'),
           (1, 1, 'And this?', 'Yet _another_ one.');

create procedure add_default_groups()
begin
    declare last_id int unsigned;

    start transaction;

    insert into test_group (config_version,
                            label,
                            probability,
                            greeting,
                            allow_previous_phase,
                            allow_previous_question,
                            allow_skip_question)
        values (1, 'control', 50, 'Hello!\n\nThis is the **control** group.', false, false, false),
               (1, 'experiment', 50, 'Hello!\n\nThis is the **experiment** group.', true, true, true);

    select last_insert_id() into last_id;

    insert into test_phase (group_id, `order`)
        values (last_id, 0),
               (last_id, 1),
               (last_id + 1, 0),
               (last_id + 1, 1);

    select last_insert_id() into last_id;

    insert into test_question (phase_id, `order`, text)
        values (last_id, 0, 'Phase 1, Question 1'),
               (last_id, 1, 'Phase 1, Question 2'),
               (last_id + 1, 0, 'Phase 2, Question 1'),
               (last_id + 1, 0, 'Phase 2, Question 2'),
               (last_id + 2, 0, 'Phase 1, Question 1'),
               (last_id + 2, 1, 'Phase 1, Question 2'),
               (last_id + 3, 0, 'Phase 2, Question 1'),
               (last_id + 3, 0, 'Phase 2, Question 2');

    select last_insert_id() into last_id;

    insert into test_option (question_id, `order`, text, correct)
        values (last_id, 0, 'Option 1', false),
               (last_id, 1, 'Option 2', true),
               (last_id + 1, 0, 'Option 1', false),
               (last_id + 1, 1, 'Option 2', true),
               (last_id + 2, 0, 'Option 1', false),
               (last_id + 2, 1, 'Option 2', true),
               (last_id + 3, 0, 'Option 1', false),
               (last_id + 3, 1, 'Option 2', true),
               (last_id + 4, 0, 'Option 1', false),
               (last_id + 4, 1, 'Option 2', true),
               (last_id + 5, 0, 'Option 1', false),
               (last_id + 5, 1, 'Option 2', true),
               (last_id + 6, 0, 'Option 1', false),
               (last_id + 6, 1, 'Option 2', true),
               (last_id + 7, 0, 'Option 1', false),
               (last_id + 7, 1, 'Option 2', true);
    commit;
end;

call add_default_groups();

drop procedure add_default_groups;

create procedure add_default_forms()
begin
    declare last_id int unsigned;

    start transaction;

    insert into form (config_version, type, title)
        values (1, 'pre', 'Pre-Test Form'),
               (1, 'post', 'Post-Test Form');

    select last_insert_id() into last_id;

    insert into form_question (form_id,
                               `order`,
                               category,
                               text,
                               type,
                               min,
                               max,
                               step,
                               other,
                               min_length,
                               max_length,
                               placeholder)
        values (last_id, 0, 'a', 'Single choice question', 'select-one', null, null, null, false, null, null, null),
               (last_id,
                1,
                'a',
                'Single choice question with "other"',
                'select-one',
                null,
                null,
                null,
                true,
                null,
                null,
                null),
               (last_id, 2, 'b', 'Multiple choice question', 'select-multiple', 2, 3, null, false, null, null, null),
               (last_id, 3, 'c', 'Slider question', 'slider', 1, 5, 1, null, null, null, null),
               (last_id + 1,
                0,
                'd',
                'Short text question',
                'text-short',
                null,
                null,
                null,
                null,
                10,
                50,
                'Placeholder 1...'),
               (last_id + 1,
                1,
                'd',
                'Long text question',
                'text-long',
                null,
                null,
                null,
                null,
                200,
                1000,
                'Placeholder 2...');

    select last_insert_id() into last_id;

    insert into form_question_option (form_question_id, `order`, text)
        values (last_id, 0, 'Option 1'),
               (last_id, 1, 'Option 2'),
               (last_id + 1, 0, 'Option 1'),
               (last_id + 1, 1, 'Option 2'),
               (last_id + 2, 0, 'Option 1'),
               (last_id + 2, 1, 'Option 2'),
               (last_id + 2, 2, 'Option 3'),
               (last_id + 2, 3, 'Option 4');

    insert into form_question_slider_label (form_question_id, number, label)
        values (last_id + 3, 1, 'Never'),
               (last_id + 3, 3, 'Sometimes'),
               (last_id + 3, 5, 'Always');
    commit;
end;

call add_default_forms();

drop procedure add_default_forms;

update translation_value
set key_id = 3
    where config_version = 1
      and key_id = 2
      and value = 'dark';

alter table translation_value
    add unique (config_version, key_id);
