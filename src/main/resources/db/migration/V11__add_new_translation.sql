insert into translation_key (`key`, description, arguments)
    values ('dashboard.configVersion', 'Column header for configuration version in the results table.', json_array());

insert into translation_value (config_version, key_id, value)
select c.version,
       tk.id,
       'Config.  version'
    from config                    c
        cross join translation_key tk
    where tk.`key` = 'dashboard.configVersion';
