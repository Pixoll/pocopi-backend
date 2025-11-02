drop trigger if exists before_update_user_test_attempt;

delimiter $$
create trigger before_update_user_test_attempt
    before update
    on user_test_attempt
    for each row
begin
    if (old.id != new.id or old.user_id != new.user_id or old.group_id != new.group_id or old.start != new.start) then
        signal sqlstate '45000' set message_text = 'only row "end" of user_test_attempt can be updated';
    end if;
end;

$$
delimiter ;
