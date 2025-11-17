alter table config
    add column active boolean not null default true;

create trigger after_insert_config
    after insert
    on config
    for each row
begin
    if (not new.active) then
        update config set active = true where version = new.version;
    end if;

    update config set active = false where version != new.version;
end;

update config c1
    inner join (
        select max(c2.version) as last_version
            from config c2
        ) as c3
set c1.active = false
    where c1.version != c3.last_version;
