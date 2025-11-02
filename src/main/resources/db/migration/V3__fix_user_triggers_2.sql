drop trigger if exists before_insert_user_form_answer;

delimiter $$
create trigger before_insert_user_form_answer
    before insert
    on user_form_answer
    for each row
begin
    declare conflict boolean;
    declare form_id int4 unsigned;
    declare q_type varchar(20);
    declare is_other boolean;
    declare min int2 unsigned;
    declare max int2 unsigned;
    declare total_answers int3 unsigned;
    declare has_answer boolean;

    select q.form_id, q.type, ifnull(q.other, false), q.min, q.max
        into form_id, q_type, is_other, min, max
        from form_question as q
        where q.id = new.question_id;

    select count(*), count(answer) > 0
        into total_answers, has_answer
        from user_form_answer as fa
        where fa.form_sub_id = new.form_sub_id
          and fa.question_id = new.question_id;

    if (q_type != 'select-multiple') then
        if (total_answers > 0) then
            signal sqlstate '45000' set message_text =
                'questions of type other than select-multiple cannot have more than one answer';
        end if;
    else
        if (total_answers + 1 > max) then
            signal sqlstate '45000' set message_text = 'too many answers';
        end if;

        if (new.option_id is not null) then
            set conflict = (select true
                                from user_form_answer as fa
                                where fa.form_sub_id = new.form_sub_id
                                  and fa.question_id = new.question_id
                                  and fa.option_id = new.option_id);

            if (conflict) then
                signal sqlstate '45000' set message_text = 'option_id cannot be repeated';
            end if;
        end if;

        if (new.answer is not null and has_answer) then
            signal sqlstate '45000' set message_text = 'answer cannot be repeated';
        end if;
    end if;

    set conflict = (select s.form_id != form_id from user_form_submission as s where s.id = new.form_sub_id);

    if (conflict) then
        signal sqlstate '45000' set message_text =
            'question_id.form_id must match form_sub_id.form_id';
    end if;

    if (new.option_id is not null) then
        set conflict = (select q.form_id != form_id
                            from form_question_option    as o
                                inner join form_question as q on q.id = o.form_question_id
                            where o.id = new.option_id);

        if (conflict) then
            signal sqlstate '45000' set message_text =
                'option_id.form_question_id.form_id must match form_sub_id.form_id';
        end if;
    end if;

    if (q_type in ('select-one', 'select-multiple')) then
        if is_other then
            -- checks if both are (not) null at the same time, only one must be present
            if ((new.answer is null) = (new.option_id is null)) then
                signal sqlstate '45000' set message_text =
                    'either answer or option_id must be present (but not both) when question_id.type is select-one or select-multiple and question_id.other is true';
            end if;
        else
            if (new.option_id is null) then
                signal sqlstate '45000' set message_text =
                    'option_id must be present when question_id.type is select-one or select-multiple and question_id.other is false';
            end if;

            if (new.answer is not null) then
                signal sqlstate '45000' set message_text =
                    'answer should not be present when question_id.type is select-one or select-multiple and question_id.other is false';
            end if;
        end if;
    elseif (new.option_id is not null) then
        signal sqlstate '45000' set message_text =
            'option_id should not be present when question_id.type is neither select-one or select-multiple';
    end if;

    if (q_type = 'slider') then
        if (new.value is null) then
            signal sqlstate '45000' set message_text = 'value must be present when question_id.type is slider';
        end if;

        if (new.value < min or new.value > max) then
            signal sqlstate '45000' set message_text = 'value must be between the min and max specified in question_id';
        end if;
    end if;

    if (q_type != 'slider' and new.value is not null) then
        signal sqlstate '45000' set message_text = 'value should not be present when question_id.type is not slider';
    end if;

    if (q_type in ('text-short', 'text-long') and new.answer is null) then
        signal sqlstate '45000' set message_text =
            'answer must be present when question_id.type is text-short or text-long';
    end if;

    if (q_type not in ('text-short', 'text-long', 'select-one', 'select-multiple') and new.answer is not null) then
        signal sqlstate '45000' set message_text =
            'answer should not be present when question_id.type is neither text-short or text-long';
    end if;
end;

$$
delimiter ;
