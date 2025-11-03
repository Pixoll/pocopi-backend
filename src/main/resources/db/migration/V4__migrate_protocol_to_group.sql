alter table test_group
    add column allow_previous_phase    bool default true,
    add column allow_previous_question bool default true,
    add column allow_skip_question     bool default true,
    add column randomize_phases        bool default false;

update test_group g
    inner join test_protocol p on g.id = p.group_id
set g.allow_previous_phase    = p.allow_previous_phase,
    g.allow_previous_question = p.allow_previous_question,
    g.allow_skip_question     = p.allow_skip_question,
    g.randomize_phases        = p.randomize_phases;

delete
    from test_phase
    where protocol_id in (select id from test_protocol where group_id is null);

alter table test_phase
    drop foreign key test_phase_ibfk_1,
    drop index protocol_id;

update test_phase ph
    inner join test_protocol pr on ph.protocol_id = pr.id
set ph.protocol_id = pr.group_id;

alter table test_phase
    change column protocol_id group_id int4 unsigned not null;

alter table test_phase
    add unique (group_id, `order`);

alter table test_phase
    add foreign key (group_id) references test_group (id) on delete cascade;

drop trigger if exists before_insert_test_protocol;

drop trigger if exists before_update_test_protocol;

drop trigger if exists before_insert_user_test_question_log;

drop trigger if exists before_insert_user_test_option_log;

delimiter $$
create trigger before_insert_user_test_question_log
    before insert
    on user_test_question_log
    for each row
begin
    declare conflict boolean;
    declare v_group_id int4 unsigned;

    set v_group_id = (select a.group_id from user_test_attempt as a where a.id = new.attempt_id);
    set conflict = (select p.group_id = v_group_id
                        from test_question        as q
                            inner join test_phase as p on p.id = q.phase_id
                        where q.id = new.question_id);

    if (conflict) then
        signal sqlstate '45000' set message_text =
            'question_id.phase_id.group_id must match attempt_id.group_id';
    end if;
end;

$$
delimiter ;

delimiter $$
create trigger before_insert_user_test_option_log
    before insert
    on user_test_option_log
    for each row
begin
    declare conflict boolean;
    declare v_group_id int4 unsigned;

    set v_group_id = (select a.group_id from user_test_attempt as a where a.id = new.attempt_id);
    set conflict = (select p.group_id = v_group_id
                        from test_option             as o
                            inner join test_question as q on o.question_id = q.id
                            inner join test_phase    as p on p.id = q.phase_id
                        where o.id = new.option_id);

    if (conflict) then
        signal sqlstate '45000' set message_text =
            'option_id.question_id.phase_id.group_id must match attempt_id.group_id';
    end if;
end;

$$
delimiter ;

drop table test_protocol;
