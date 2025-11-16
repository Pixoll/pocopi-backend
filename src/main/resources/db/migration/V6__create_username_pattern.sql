create table pattern (
    id    int4 unsigned primary key not null auto_increment,
    name  varchar(50) unique        not null check (name != ''),
    regex varchar(50)               not null check (regex != '')
);

insert into pattern (name, regex)
    values ('RUN (Chile)', '^\\d{8,9}-[\\dkK]$'),
           ('Matrícula (Universidad de Concepción)', '^\\d{10}(-\\d)?$');

alter table config
    add column username_pattern_id int4 unsigned default null,
    add foreign key (username_pattern_id)
        references pattern (id)
        on delete set null;
