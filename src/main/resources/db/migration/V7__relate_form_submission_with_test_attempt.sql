alter table user_form_submission
    drop constraint user_form_submission_ibfk_1,
    drop index user_id,
    drop column user_id,
    add column attempt_id int8 unsigned not null after id,
    add foreign key (attempt_id)
        references user_test_attempt (id)
        on delete cascade,
    add unique (attempt_id, form_id);

create trigger before_insert_user_form_submission
    before insert
    on user_form_submission
    for each row
begin
    declare conflict boolean default false;

    set conflict = (
        select f.config_version != g.config_version
            from user_form_submission        as s
                inner join user_test_attempt as ta on ta.id = s.attempt_id
                inner join form              as f on f.id = s.form_id
                inner join test_group        as g on g.id = ta.group_id
        );

    if (conflict) then
        signal sqlstate '45000' set message_text =
            'form_id.config_version must match attempt_id.group_id.config_version';
    end if;
end;
