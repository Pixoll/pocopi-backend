drop table if exists home_info_card;

drop table if exists home_faq;

drop table if exists form_question_option;

drop table if exists form_question_slider_label;

drop table if exists user_form_answer;

drop table if exists form_question;

drop table if exists form;

drop table if exists user_test_option_log;

drop table if exists test_option;

drop table if exists user_test_question_log;

drop table if exists test_question;

drop table if exists test_phase;

drop table if exists test_protocol;

drop table if exists user;

drop table if exists test_group;

drop table if exists translation;

drop table if exists config;

drop table if exists image;

drop table if exists admin;

create table image (
    id   int4 unsigned primary key not null auto_increment,
    path varchar(512) unique       not null check (path != ''),
    alt  varchar(100) default null check (alt is null or alt != '')
);

create table config (
    version          int4 unsigned primary key not null auto_increment,
    icon_id          int4 unsigned                      default null,
    title            varchar(100)              not null check (title != ''),
    subtitle         varchar(200)                       default null check (subtitle is null or subtitle != ''),
    description      varchar(2000)             not null check (description != ''),
    informed_consent varchar(2000)             not null check (informed_consent != ''),
    anonymous        bool                      not null default true,
    foreign key (icon_id) references image (id) on delete restrict
);

create table translation (
    id             int4 unsigned primary key not null auto_increment,
    config_version int4 unsigned             not null,
    `key`          varchar(50)               not null check (`key` != ''),
    value          varchar(200)              not null check (value != ''),
    unique (config_version, `key`),
    foreign key (config_version) references config (version) on delete cascade
);

create table home_info_card (
    id             int4 unsigned primary key not null auto_increment,
    config_version int4 unsigned             not null,
    `order`        int1 unsigned             not null,
    title          varchar(50)               not null check (title != ''),
    description    varchar(100)              not null check (description != ''),
    icon_id        int4 unsigned default null,
    color          int3 unsigned default null,
    unique (config_version, `order`),
    foreign key (config_version) references config (version) on delete cascade,
    foreign key (icon_id) references image (id) on delete restrict
);

create table home_faq (
    id             int4 unsigned primary key not null auto_increment,
    config_version int4 unsigned             not null,
    `order`        int1 unsigned             not null,
    question       varchar(100)              not null check (question != ''),
    answer         varchar(500)              not null check (answer != ''),
    unique (config_version, `order`),
    foreign key (config_version) references config (version) on delete cascade,
    foreign key (config_version) references config (version) on delete cascade
);

create table form (
    id             int4 unsigned primary key not null auto_increment,
    config_version int4 unsigned             not null,
    type           enum ('pre', 'post')      not null,
    title          varchar(100) default null check (title is null or title != ''),
    unique (config_version, type),
    foreign key (config_version) references config (version) on delete cascade
);

create table form_question (
    id          int4 unsigned primary key not null auto_increment,
    form_id     int4 unsigned             not null,
    `order`     int1 unsigned             not null,
    category    varchar(50)               not null check (category != ''),
    text        varchar(100)                       default null check (text is null or text != ''),
    image_id    int4 unsigned                      default null,
    required    bool                      not null default true,
    type        enum (
        'select-one',
        'select-multiple',
        'slider',
        'text-short',
        'text-long'
        )                                 not null,
    min         int2 unsigned                      default null,
    max         int2 unsigned                      default null,
    step        int2 unsigned                      default null,
    other       bool                               default null,
    min_length  int2 unsigned                      default null,
    max_length  int2 unsigned                      default null check (max_length is null or max_length <= 1000),
    placeholder varchar(50)                        default null,
    unique (form_id, `order`),
    check (
        (type in ('select-one', 'select-multiple') and other is not null)
            or (type not in ('select-one', 'select-multiple') and other is null)
        ),
    check (
        (type in ('select-multiple', 'slider') and min is not null and max is not null and min <= max)
            or (type not in ('select-multiple', 'slider') and min is null and max is null)
        ),
    check (
        (type = 'slider' and step is not null)
            or (type != 'slider' and step is null)
        ),
    check (
        (type in ('text-short', 'text-long')
            and min_length is not null and max_length is not null and placeholder is not null
            and min_length <= max_length)
            or
        (type not in ('text-short', 'text-long')
            and min_length is null and max_length is null and placeholder is null)
        ),
    foreign key (form_id) references form (id) on delete cascade,
    foreign key (image_id) references image (id) on delete restrict
);

delimiter $$
create trigger before_insert_form_question
    before insert
    on form_question
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

$$
delimiter ;

delimiter $$
create trigger before_update_form_question
    before update
    on form_question
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

$$
delimiter ;

create table form_question_option (
    id               int4 unsigned primary key not null auto_increment,
    form_question_id int4 unsigned             not null,
    `order`          int1 unsigned             not null,
    text             varchar(100)  default null check (text is null or text != ''),
    image_id         int4 unsigned default null,
    unique (form_question_id, `order`),
    foreign key (form_question_id) references form_question (id) on delete cascade,
    foreign key (image_id) references image (id) on delete restrict
);

delimiter $$
create trigger before_insert_form_question_option
    before insert
    on form_question_option
    for each row
begin
    declare q_type varchar(20);
    declare msg varchar(120);

    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;

    set q_type = (select q.type from form_question as q where q.id = new.form_question_id);

    if (q_type not in ('select-one', 'select-multiple')) then
        set msg = concat(
            'form_question_id must be that of a form question with type select-one or select-multiple. got type ',
            q_type);
        signal sqlstate '45000' set message_text = '';
    end if;
end;

$$
delimiter ;

delimiter $$
create trigger before_update_form_question_option
    before update
    on form_question_option
    for each row
begin
    declare q_type varchar(20);
    declare msg varchar(120);

    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;

    if (old.form_question_id != new.form_question_id) then
        set q_type = (select q.type from form_question as q where q.id = new.form_question_id);

        if (q_type not in ('select-one', 'select-multiple')) then
            set msg = concat(
                'form_question_id must be that of a form question with type select-one or select-multiple. got type ',
                q_type);
            signal sqlstate '45000' set message_text = '';
        end if;
    end if;
end;

$$
delimiter ;

create table form_question_slider_label (
    id               int4 unsigned primary key not null auto_increment,
    form_question_id int4 unsigned             not null,
    number           int1 unsigned             not null,
    label            varchar(50)               not null check (label != ''),
    unique (form_question_id, number),
    foreign key (form_question_id) references form_question (id) on delete cascade
);

delimiter $$
create trigger before_insert_form_question_slider_label
    before insert
    on form_question_slider_label
    for each row
begin
    declare q_type varchar(20);
    declare msg varchar(120);

    set q_type = (select q.type from form_question as q where q.id = new.form_question_id);

    if (q_type != 'slider') then
        set msg = concat(
            'form_question_id must be that of a form question with type slider. got type ', q_type);
        signal sqlstate '45000' set message_text = '';
    end if;
end;

$$
delimiter ;

delimiter $$
create trigger before_update_form_question_slider_label
    before update
    on form_question_slider_label
    for each row
begin
    declare q_type varchar(20);
    declare msg varchar(120);

    if (old.form_question_id != new.form_question_id) then
        set q_type = (select q.type from form_question as q where q.id = new.form_question_id);

        if (q_type != 'slider') then
            set msg = concat(
                'form_question_id must be that of a form question with type slider. got type ', q_type);
            signal sqlstate '45000' set message_text = '';
        end if;
    end if;
end;

$$
delimiter ;

create table test_group (
    id             int4 unsigned primary key not null auto_increment,
    config_version int4 unsigned             not null,
    label          varchar(25)               not null check (label != ''),
    # further integrity checks for probability must be done outside mysql, sadly
    probability    int1 unsigned             not null check (probability between 0 and 100),
    greeting       varchar(2000) default null check (greeting is null or greeting != ''),
    unique (config_version, label),
    foreign key (config_version) references config (version) on delete cascade
);

create table test_protocol (
    id                      int4 unsigned primary key not null auto_increment,
    config_version          int4 unsigned             not null,
    label                   varchar(25)               not null check (label != ''),
    group_id                int4 unsigned default null,
    allow_previous_phase    bool          default true,
    allow_previous_question bool          default true,
    allow_skip_question     bool          default true,
    randomize_phases        bool          default false,
    unique (config_version, label),
    foreign key (config_version) references config (version) on delete cascade,
    foreign key (group_id) references test_group (id) on delete set null
);

create table test_phase (
    id                  int4 unsigned primary key not null auto_increment,
    protocol_id         int4 unsigned             not null,
    `order`             int1 unsigned             not null,
    randomize_questions bool default false,
    unique (protocol_id, `order`),
    foreign key (protocol_id) references test_protocol (id) on delete cascade
);

create table test_question (
    id                int4 unsigned primary key not null auto_increment,
    phase_id          int4 unsigned             not null,
    `order`           int1 unsigned             not null,
    text              varchar(100)  default null check (text is null or text != ''),
    image_id          int4 unsigned default null,
    randomize_options bool          default false,
    unique (phase_id, `order`),
    foreign key (phase_id) references test_phase (id) on delete cascade,
    foreign key (image_id) references image (id) on delete restrict
);

delimiter $$
create trigger before_insert_test_question
    before insert
    on test_question
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

$$
delimiter ;

delimiter $$
create trigger before_update_test_question
    before update
    on test_question
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

$$
delimiter ;

create table test_option (
    id          int4 unsigned primary key not null auto_increment,
    question_id int4 unsigned             not null,
    `order`     int1 unsigned             not null,
    text        varchar(100)  default null check (text is null or text != ''),
    image_id    int4 unsigned default null,
    correct     bool                      not null,
    unique (question_id, `order`),
    foreign key (question_id) references test_question (id) on delete cascade,
    foreign key (image_id) references image (id) on delete restrict
);

delimiter $$
create trigger before_insert_test_option
    before insert
    on test_option
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

$$
delimiter ;

delimiter $$
create trigger before_update_test_option
    before update
    on test_option
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

$$
delimiter ;

create table user (
    id        int4 unsigned primary key not null auto_increment,
    username  varchar(32) unique        not null check (username != ''),
    group_id  int4 unsigned             not null,
    anonymous bool                      not null,
    name      varchar(50)        default null,
    email     varchar(50) unique default null,
    age       int1 unsigned      default null check (age is null or age <= 120),
    password  char(60)                  not null check (password != ''),
    index (group_id),
    check (
        (anonymous = true and name is null and email is null and age is null)
            or (anonymous = false and name is not null and email is not null and age is not null)
        ),
    foreign key (group_id) references test_group (id) on delete restrict
);

create table admin (
    id       int4 unsigned primary key not null auto_increment,
    username varchar(32) unique        not null check (username != ''),
    password char(60)                  not null check (password != '')
);

create table user_form_answer (
    id          int4 unsigned primary key not null auto_increment,
    user_id     int4 unsigned             not null,
    question_id int4 unsigned             not null,
    answer      varchar(1000)             not null check (answer != ''),
    unique (user_id, question_id),
    foreign key (user_id) references user (id) on delete cascade,
    foreign key (question_id) references form_question (id) on delete restrict
);

create table user_test_question_log (
    id          int8 unsigned primary key not null auto_increment,
    user_id     int4 unsigned             not null,
    question_id int4 unsigned             not null,
    start       timestamp                 not null,
    end         timestamp                 not null,
    unique (user_id, question_id),
    check (start <= end),
    foreign key (user_id) references user (id) on delete cascade,
    foreign key (question_id) references test_question (id) on delete restrict
);

create table user_test_option_log (
    id        int8 unsigned primary key            not null auto_increment,
    user_id   int4 unsigned                        not null,
    option_id int4 unsigned                        not null,
    type      enum ('deselect', 'select', 'hover') not null,
    timestamp timestamp                            not null,
    unique (user_id, option_id, type, timestamp),
    foreign key (user_id) references user (id) on delete cascade,
    foreign key (option_id) references test_option (id) on delete restrict
);
