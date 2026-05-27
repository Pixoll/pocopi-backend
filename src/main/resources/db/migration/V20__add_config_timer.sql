alter table config
    add column timer time default null check (timer is null or timer > 0) after username_pattern_id;
