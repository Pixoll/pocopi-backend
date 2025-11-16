drop trigger if exists before_insert_user_test_question_log;

drop trigger if exists before_insert_user_test_option_log;

create trigger before_insert_user_test_question_log
    before insert
    on user_test_question_log
    for each row
begin
    declare conflict boolean;
    declare v_group_id int4 unsigned;

    set v_group_id = (select a.group_id from user_test_attempt as a where a.id = new.attempt_id);
    set conflict = (select p.group_id != v_group_id
                        from test_question        as q
                            inner join test_phase as p on p.id = q.phase_id
                        where q.id = new.question_id);

    if (conflict) then
        signal sqlstate '45000' set message_text =
            'question_id.phase_id.group_id must match attempt_id.group_id';
    end if;
end;

create trigger before_insert_user_test_option_log
    before insert
    on user_test_option_log
    for each row
begin
    declare conflict boolean;
    declare v_group_id int4 unsigned;

    set v_group_id = (select a.group_id from user_test_attempt as a where a.id = new.attempt_id);
    set conflict = (select p.group_id != v_group_id
                        from test_option             as o
                            inner join test_question as q on o.question_id = q.id
                            inner join test_phase    as p on p.id = q.phase_id
                        where o.id = new.option_id);

    if (conflict) then
        signal sqlstate '45000' set message_text =
            'option_id.question_id.phase_id.group_id must match attempt_id.group_id';
    end if;
end;
