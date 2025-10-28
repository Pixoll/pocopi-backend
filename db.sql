drop table if exists home_info_card;

drop table if exists home_faq;

drop table if exists user_form_answer;

drop table if exists user_form_submission;

drop table if exists form_question_option;

drop table if exists form_question_slider_label;

drop table if exists form_question;

drop table if exists form;

drop table if exists user_test_option_log;

drop table if exists test_option;

drop table if exists user_test_question_log;

drop table if exists test_question;

drop table if exists test_phase;

drop table if exists test_protocol;

drop table if exists user_test_attempt;

drop table if exists user;

drop table if exists test_group;

drop table if exists translation_value;

drop table if exists translation_key;

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

create table translation_key (
    id          int4 unsigned primary key not null auto_increment,
    `key`       varchar(50) unique        not null check (`key` != ''),
    description varchar(500)              not null check (description != ''),
    arguments   json                      not null check (json_schema_valid('{
      "type": "array",
      "items": {
        "type": "string"
      }
    }', arguments))
);

create table translation_value (
    id             int8 unsigned primary key not null auto_increment,
    config_version int4 unsigned             not null,
    key_id         int4 unsigned             not null,
    value          varchar(200)              not null check (value != ''),
    foreign key (config_version) references config (version) on delete cascade,
    foreign key (key_id) references translation_key (id) on delete cascade
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
    text        varchar(200)                       default null check (text is null or text != ''),
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
        signal sqlstate '45000' set message_text = msg;
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
            signal sqlstate '45000' set message_text = msg;
        end if;
    end if;
end;

$$
delimiter ;

create table form_question_slider_label (
    id               int4 unsigned primary key not null auto_increment,
    form_question_id int4 unsigned             not null,
    number           int2 unsigned             not null,
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
        signal sqlstate '45000' set message_text = msg;
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
            signal sqlstate '45000' set message_text = msg;
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

delimiter $$
create trigger before_insert_test_protocol
    before insert
    on test_protocol
    for each row
begin
    declare config_version int4 unsigned;

    if (new.group_id is not null) then
        set config_version = (select g.config_version from test_group as g where g.id = new.group_id);

        if (config_version != new.config_version) then
            signal sqlstate '45000' set message_text =
                'group_id.config_version must match test_protocol.config_version';
        end if;
    end if;
end;

$$
delimiter ;

delimiter $$
create trigger before_update_test_protocol
    before update
    on test_protocol
    for each row
begin
    declare config_version int4 unsigned;

    if (new.group_id is not null and old.group_id != new.group_id) then
        set config_version = (select g.config_version from test_group as g where g.id = new.group_id);

        if (config_version != new.config_version) then
            signal sqlstate '45000' set message_text =
                'group_id.config_version must match test_protocol.config_version';
        end if;
    end if;
end;

$$
delimiter ;

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
    role      enum ('USER', 'ADMIN')    not null default 'USER',
    anonymous bool                      not null,
    name      varchar(50)                        default null,
    email     varchar(50) unique                 default null,
    age       int1 unsigned                      default null check (age is null or age <= 120),
    password  char(60)                  not null check (password != ''),
    check (
        (anonymous and name is null and email is null and age is null)
            or (not anonymous and name is not null and email is not null and age is not null)
        )
);

create table user_form_submission (
    id        int4 unsigned primary key not null auto_increment,
    user_id   int4 unsigned             not null,
    form_id   int4 unsigned             not null,
    timestamp datetime(3)               not null,
    unique (user_id, form_id, timestamp),
    foreign key (user_id) references user (id) on delete cascade,
    foreign key (form_id) references form (id) on delete restrict
);

delimiter $$
create trigger before_update_user_form_submission
    before update
    on user_form_submission
    for each row
begin
    signal sqlstate '45000' set message_text = 'user_form_submission\'s rows cannot be updated';
end;

$$
delimiter ;

create table user_form_answer (
    id          int4 unsigned primary key not null auto_increment,
    form_sub_id int4 unsigned             not null,
    question_id int4 unsigned             not null,
    option_id   int4 unsigned default null,
    value       int2 unsigned default null,
    answer      varchar(1000) default null check (answer is null or answer != ''),
    index (form_sub_id, question_id),
    foreign key (form_sub_id) references user_form_submission (id) on delete cascade,
    foreign key (question_id) references form_question (id) on delete restrict,
    foreign key (option_id) references form_question_option (id) on delete restrict
);

delimiter $$
create trigger before_insert_user_form_answer
    before insert
    on user_form_answer
    for each row
begin
    declare conflict boolean;
    declare form_id int4 unsigned;
    declare q_type varchar(20);
    declare is_other bool;
    declare min int2 unsigned;
    declare max int2 unsigned;

    select q.form_id, q.type, ifnull(q.other, false), q.min, q.max
        into form_id, q_type, is_other, min, max
        from form_question as q
        where q.id = new.question_id;

    if (q_type != 'select-multiple') then
        set conflict = (select true
                            from user_form_answer as fa
                            where fa.form_sub_id = new.form_sub_id
                              and fa.question_id = new.question_id);

        if (conflict) then
            signal sqlstate '45000' set message_text =
                'questions of type other than select-multiple cannot have more than one answer';
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
            if (new.answer is null) then
                signal sqlstate '45000' set message_text =
                    'answer must be present when question_id.type is select-one or select-multiple and question_id.other is true';
            end if;

            if (new.option_id is not null) then
                signal sqlstate '45000' set message_text =
                    'option_id should not be present when question_id.type is select-one or select-multiple and question_id.other is true';
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

delimiter $$
create trigger before_update_user_form_answer
    before update
    on user_form_answer
    for each row
begin
    signal sqlstate '45000' set message_text = 'user_form_answer\'s rows cannot be updated';
end;

$$
delimiter ;

create table user_test_attempt (
    id       int8 unsigned primary key not null auto_increment,
    user_id  int4 unsigned             not null,
    group_id int4 unsigned             not null,
    start    datetime(3)               not null,
    end      datetime(3) default null,
    unique (user_id, group_id, start),
    foreign key (user_id) references user (id) on delete cascade,
    foreign key (group_id) references test_group (id) on delete restrict
);

delimiter $$
create trigger before_update_user_test_attempt
    before update
    on user_test_attempt
    for each row
begin
    signal sqlstate '45000' set message_text = 'user_test_attempt\'s rows cannot be updated';
end;

create table user_test_question_log (
    id          int8 unsigned primary key not null auto_increment,
    attempt_id  int8 unsigned             not null,
    question_id int4 unsigned             not null,
    timestamp   datetime(3)               not null,
    duration    int4 unsigned             not null,
    unique (attempt_id, question_id, timestamp),
    foreign key (attempt_id) references user_test_attempt (id) on delete cascade,
    foreign key (question_id) references test_question (id) on delete restrict
);

delimiter $$
create trigger before_insert_user_test_question_log
    before insert
    on user_test_question_log
    for each row
begin
    declare conflict boolean;
    declare group_id int4 unsigned;

    set group_id = (select a.group_id from user_test_attempt as a where a.id = new.attempt_id);
    set conflict = (select pr.group_id = group_id
                        from test_question           as q
                            inner join test_phase    as ph on ph.id = q.phase_id
                            inner join test_protocol as pr on pr.id = ph.protocol_id
                        where q.id = new.question_id);

    if (conflict) then
        signal sqlstate '45000' set message_text =
            'question_id.phase_id.protocol_id.group_id must match attempt_id.group_id';
    end if;
end;

$$
delimiter ;

delimiter $$
create trigger before_update_user_test_question_log
    before update
    on user_test_question_log
    for each row
begin
    signal sqlstate '45000' set message_text = 'user_test_question_log\'s rows cannot be updated';
end;

$$
delimiter ;

create table user_test_option_log (
    id         int8 unsigned primary key            not null auto_increment,
    attempt_id int8 unsigned                        not null,
    option_id  int4 unsigned                        not null,
    type       enum ('deselect', 'select', 'hover') not null,
    timestamp  datetime(3)                          not null,
    unique (attempt_id, option_id, type, timestamp),
    foreign key (attempt_id) references user_test_attempt (id) on delete cascade,
    foreign key (option_id) references test_option (id) on delete restrict
);

delimiter $$
create trigger before_insert_user_test_option_log
    before insert
    on user_test_option_log
    for each row
begin
    declare conflict boolean;
    declare group_id int4 unsigned;

    set group_id = (select a.group_id from user_test_attempt as a where a.id = new.attempt_id);
    set conflict = (select pr.group_id = group_id
                        from test_option             as o
                            inner join test_question as q on o.question_id = q.id
                            inner join test_phase    as ph on ph.id = q.phase_id
                            inner join test_protocol as pr on pr.id = ph.protocol_id
                        where o.id = new.option_id);

    if (conflict) then
        signal sqlstate '45000' set message_text =
            'option_id.question_id.phase_id.protocol_id.group_id must match attempt_id.group_id';
    end if;
end;

$$
delimiter ;

delimiter $$
create trigger before_update_user_test_option_log
    before update
    on user_test_option_log
    for each row
begin
    signal sqlstate '45000' set message_text = 'user_test_option_log\'s rows cannot be updated';
end;

$$
delimiter ;

insert into image (path, alt)
    values ('images/icons/app.png', 'App Icon'),
           ('images/icons/flask.svg', 'Flask Icon'),
           ('images/icons/file_signature.svg', 'File Signature Icon'),
           ('images/saliente/practice/1.jpg', 'question image PRACTICA_SALIENTE_1'),
           ('images/saliente/practice/2.jpg', 'question image PRACTICA_SALIENTE_2'),
           ('images/saliente/practice/3.jpg', 'question image PRACTICA_SALIENTE_3'),
           ('images/saliente/questions/E1.jpg', 'question image SALIENTE_E1'),
           ('images/saliente/questions/E2.jpg', 'question image SALIENTE_E2'),
           ('images/saliente/questions/E3.jpg', 'question image SALIENTE_E3'),
           ('images/saliente/questions/E4.jpg', 'question image SALIENTE_E4'),
           ('images/saliente/questions/E5.jpg', 'question image SALIENTE_E5'),
           ('images/saliente/questions/E6.jpg', 'question image SALIENTE_E6'),
           ('images/saliente/questions/E7.jpg', 'question image SALIENTE_E7'),
           ('images/saliente/questions/E8.jpg', 'question image SALIENTE_E8'),
           ('images/saliente/questions/E9.jpg', 'question image SALIENTE_E9'),
           ('images/saliente/questions/E10.jpg', 'question image SALIENTE_E10'),
           ('images/contextual/practice/1.jpg', 'question image PRACTICA_CONTEXTUAL_1'),
           ('images/contextual/practice/2.jpg', 'question image PRACTICA_CONTEXTUAL_2'),
           ('images/contextual/practice/3.jpg', 'question image PRACTICA_CONTEXTUAL_3'),
           ('images/contextual/questions/E1.jpg', 'question image CONTEXTUAL_E1'),
           ('images/contextual/questions/E2.jpg', 'question image CONTEXTUAL_E2'),
           ('images/contextual/questions/E3.jpg', 'question image CONTEXTUAL_E3'),
           ('images/contextual/questions/E4.jpg', 'question image CONTEXTUAL_E4'),
           ('images/contextual/questions/E5.jpg', 'question image CONTEXTUAL_E5'),
           ('images/contextual/questions/E6.jpg', 'question image CONTEXTUAL_E6'),
           ('images/contextual/questions/E7.jpg', 'question image CONTEXTUAL_E7'),
           ('images/contextual/questions/E8.jpg', 'question image CONTEXTUAL_E8'),
           ('images/contextual/questions/E9.jpg', 'question image CONTEXTUAL_E9'),
           ('images/contextual/questions/E10.jpg', 'question image CONTEXTUAL_E10');

insert into config (icon_id, title, subtitle, description, informed_consent, anonymous)
    values (1,
            'Raven\'s Progressive Matrices Test',
            'An assessment to measure analytical reasoning and problem-solving skills.',
            'Raven\'s Progressive Matrices Test is one of the most widely used tools for assessing non-verbal intelligence and reasoning. In this test, you will be presented with a series of matrices or designs with a missing part. Your task will be to select, from several options, the one that correctly completes the pattern.\n',
            'By agreeing to participate in this study, you acknowledge that:\n- Your participation is completely voluntary.\n- The data provided will be treated with confidentiality.\n- The information collected will be used solely for academic purposes.\n- You can leave the test at any time if you wish.\n\nThe test has no time limit, but it is recommended to complete it without interruptions. If you have any questions about the study, you can contact the research team at [research@example.com](mailto:research@example.com).\n',
            default);

insert into translation_key (`key`, description, arguments)
    values ('home.dashboardButtonHint',
            'Tooltip to show when hovering over the dashboard button in the home page.',
            json_array()),
           ('home.themeSwitchButtonHint',
            'Tooltip to show when hovering over the theme toggle button in the home page.',
            json_array('Either "dark" or "light".')),
           ('home.participant',
            'The name and username/id of the participant. Displayed on the home page.',
            json_array('The name of the participant.', 'The username/id of the participant.')),
           ('home.aboutThisTest', 'Heading for the test description section of the home page.', json_array()),
           ('home.informedConsent', 'Heading for the informed consent section of the home page.', json_array()),
           ('home.iAcceptInformedConsent', 'Label for the checkbox to accept the informed consent.', json_array()),
           ('home.startTest', 'Button text to start the test.', json_array()),
           ('home.register', 'Button text to open the registration modal.', json_array()),
           ('home.frequentlyAskedQuestions', 'Heading for the FAQ section.', json_array()),
           ('home.pleaseEnterValid',
            'Validation error message for invalid input fields.',
            json_array('The label of the invalid field.')),
           ('home.participantInformation',
            'Heading for the participant information section in the registration modal.',
            json_array()),
           ('home.registrationModalMessage', 'Privacy message displayed in the registration modal.', json_array()),
           ('home.fullName', 'Label for the full name input field.', json_array()),
           ('home.identificationNumber', 'Label for the username/id input field.', json_array()),
           ('home.age', 'Label for the age input field.', json_array()),
           ('home.email', 'Label for the email input field.', json_array()),
           ('home.cancel', 'Button text to cancel the registration modal.', json_array()),
           ('home.saveInformation',
            'Button text to save the participant information in the registration modal.',
            json_array()),
           ('form.otherPlaceholder', 'Placeholder text for "other" or custom input fields in forms.', json_array()),
           ('form.youMustAnswerEverything',
            'Validation message shown when not all required questions have been answered.',
            json_array()),
           ('form.sendAnswers', 'Button text to submit form answers.', json_array()),
           ('form.sendingAnswers', 'Loading message displayed while submitting form answers.', json_array()),
           ('preTest.title', 'Title for the pre-test form page.', json_array()),
           ('postTest.title', 'Title for the post-test form page.', json_array()),
           ('greeting.title', 'Title for the greeting/information page before starting the test.', json_array()),
           ('greeting.startTest', 'Button text to start the test from the greeting page.', json_array()),
           ('test.progress', 'Label for the progress indicator during the test.', json_array()),
           ('test.phaseQuestion',
            'Label showing the current phase and question number during the test.',
            json_array('The current phase.', 'The total number of phases.', 'The current question',
                       'The total number of questions in the current phase.')),
           ('test.previousPhase', 'Button text to go back to the previous phase.', json_array()),
           ('test.previousQuestion', 'Button text to go to the previous question.', json_array()),
           ('test.nextQuestion', 'Button text to go to the next question.', json_array()),
           ('test.backToSummary', 'Button text to return to the summary page.', json_array()),
           ('summary.phaseSummary', 'Heading for the phase summary page.', json_array('The current phase.')),
           ('summary.testSummary', 'Resumen del Test', json_array()),
           ('summary.nextPhase', 'Continuar a la siguiente fase', json_array()),
           ('summary.endTest', 'Terminar test', json_array()),
           ('summary.phase', 'Fase', json_array()),
           ('summary.question', 'Pregunta', json_array()),
           ('summary.status', 'Estado', json_array()),
           ('summary.answered', 'Respondida', json_array()),
           ('summary.notAnswered', 'Sin responder', json_array()),
           ('completion.testCompleted', '¡Test Completado!', json_array()),
           ('completion.successfullySubmitted', 'Success message shown after test submission.', json_array()),
           ('completion.thankYou',
            'Thank you message displayed to the participant on the completion page.',
            json_array('The name or username/id of the participant.')),
           ('completion.successfullyCompleted',
            'Success message indicating the test has been successfully submitted.',
            json_array('The name of the application, obtained from the title of the latest configuration.')),
           ('completion.userInfo', 'Heading for the user information section on the completion page.', json_array()),
           ('completion.name', 'Label for the participant name on the completion page.', json_array()),
           ('completion.identification', 'Label for the participant username/id on the completion page.', json_array()),
           ('completion.email', 'Label for the participant email on the completion page.', json_array()),
           ('completion.results', 'Heading for the test results section on the completion page.', json_array()),
           ('completion.viewResults', 'Button text to show the test results.', json_array()),
           ('completion.hideResults', 'Button text to hide the test results.', json_array()),
           ('completion.gettingResults', 'Loading message while fetching test results.', json_array()),
           ('completion.failedToGetResults',
            'Error message shown when test results cannot be retrieved.',
            json_array()),
           ('completion.noResultsFound', 'Message shown when no results are available.', json_array()),
           ('completion.correctAnswers', 'Label for the number of correct answers in the results.', json_array()),
           ('completion.correctOfTotal',
            'Text showing the number of correct answers out of total questions.',
            json_array('How many questions the participant answered correctly.', 'The total number of questions.')),
           ('completion.skippedQuestions', 'Label for the number of skipped questions in the results.', json_array()),
           ('completion.accuracyPercent', 'Label for the accuracy percentage in the results.', json_array()),
           ('completion.timeTaken', 'Label for the total time taken to complete the test.', json_array()),
           ('completion.timeSeconds',
            'Text displaying the time taken in seconds.',
            json_array('How long the participant took to answer the test, in seconds.')),
           ('completion.resultsRecorded',
            'Confirmation message that results have been saved, with privacy notice.',
            json_array()),
           ('completion.backToHome', 'Button text to return to the home page from the completion page.', json_array()),
           ('dashboard.loadingResults',
            'Loading message displayed while fetching test results in the dashboard.',
            json_array()),
           ('dashboard.analytics',
            'Title for the analytics/dashboard page.',
            json_array('The name of the application, obtained from the title of the latest configuration.')),
           ('dashboard.backToHome', 'Button text to return to the home page from the dashboard.', json_array()),
           ('dashboard.viewAndExportResults',
            'Description text explaining the purpose of the dashboard.',
            json_array()),
           ('dashboard.participantsList', 'Heading for the participants list section.', json_array()),
           ('dashboard.testResults', 'Heading for the test results section.', json_array()),
           ('dashboard.exportCsv', 'Button text to export results as a CSV file.', json_array()),
           ('dashboard.noResults', 'Message shown when there are no test results available yet.', json_array()),
           ('dashboard.participant', 'Column header for participant name in the results table.', json_array()),
           ('dashboard.group', 'Column header for group in the results table.', json_array()),
           ('dashboard.date', 'Column header for date in the results table.', json_array()),
           ('dashboard.timeTaken', 'Column header for time taken (in seconds) in the results table.', json_array()),
           ('dashboard.correct', 'Column header for number of correct answers in the results table.', json_array()),
           ('dashboard.answered', 'Column header for number of answered questions in the results table.', json_array()),
           ('dashboard.accuracy', 'Column header for accuracy percentage in the results table.', json_array()),
           ('dashboard.actions', 'Column header for action buttons in the results table.', json_array()),
           ('dashboard.id',
            'Label displaying the participant username/id.',
            json_array('The username/id of the participant.')),
           ('dashboard.exportParticipantResult',
            'Button text or tooltip to export detailed results for a specific participant.',
            json_array()),
           ('dashboard.summary', 'Heading for the summary statistics section.', json_array()),
           ('dashboard.totalParticipants', 'Label for the total number of participants statistic.', json_array()),
           ('dashboard.averageAccuracy', 'Label for the average accuracy statistic.', json_array()),
           ('dashboard.averageTimeTaken', 'Label for the average time taken statistic.', json_array()),
           ('dashboard.totalQuestionsAnswered', 'Label for the total questions answered statistic.', json_array()),
           ('dashboard.errorLoadingResults',
            'Error message shown when results fail to load in the dashboard.',
            json_array()),
           ('dashboard.errorNoResults',
            'Message shown when no results are found, explaining that users need to complete the test first.',
            json_array()),
           ('dashboard.errorExportCsv', 'Error message shown when CSV export fails.', json_array()),
           ('dashboard.errorExportUser',
            'Error message shown when exporting a specific participant\'s data fails.',
            json_array('The username/id of the participant.')),
           # TODO remove these eventually
           ('backend.anonymousUser', 'Default name for anonymous users.', json_array()),
           ('backend.userDoesNotExist',
            'Error message when trying to access a user that does not exist.',
            json_array('The username/id of the participant.')),
           ('backend.userAlreadyExists',
            'Error message when trying to create a user that already exists.',
            json_array('The username/id of the participant.'));

insert into translation_value (config_version, key_id, value)
    values (1, 1, 'Panel de Administración'),
           (1, 2, 'Cambiar a modo {0}'),
           (1, 3, 'Participante: {0} ({1})'),
           (1, 4, 'Sobre este test'),
           (1, 5, 'Consentimiento Informado'),
           (1, 6, 'He leído y acepto el consentimiento informado'),
           (1, 7, 'Iniciar Test'),
           (1, 8, 'Registrarse'),
           (1, 9, 'Preguntas Frecuentes'),
           (1, 10, 'Por favor ingrese un {0} válido.'),
           (1, 11, 'Información del Participante'),
           (1,
            12,
            'Tus datos serán tratados confidencialmente y serán utilizados exclusivamente con propósitos académicos.'),
           (1, 13, 'Nombre Completo'),
           (1, 14, 'Número de Identificación'),
           (1, 15, 'Edad'),
           (1, 16, 'Correo Electrónico'),
           (1, 17, 'Cancelar'),
           (1, 18, 'Guardar Información'),
           (1, 19, 'Especifique...'),
           (1, 20, 'Debes responder todas las preguntas.'),
           (1, 21, 'Enviar respuestas'),
           (1, 22, 'Enviando respuestas...'),
           (1, 23, 'Formulario Pre-Test'),
           (1, 24, 'Formulario Post-Test'),
           (1, 25, 'Información del Test'),
           (1, 26, 'Iniciar Test'),
           (1, 27, 'Progreso:'),
           (1, 28, 'Fase {0}/{1} - Pregunta {2}/{3}'),
           (1, 29, 'Fase Anterior'),
           (1, 30, 'Anterior'),
           (1, 31, 'Siguiente'),
           (1, 32, 'Volver al resumen'),
           (1, 33, 'Resumen de la Fase {0}'),
           (1, 34, 'Resumen del Test'),
           (1, 35, 'Continuar a la siguiente fase'),
           (1, 36, 'Terminar test'),
           (1, 37, 'Fase'),
           (1, 38, 'Pregunta'),
           (1, 39, 'Estado'),
           (1, 40, 'Respondida'),
           (1, 41, 'Sin responder'),
           (1, 42, '¡Test Completado!'),
           (1, 43, 'Enviado Exitosamente'),
           (1, 44, 'Muchas gracias {0} por tu participación'),
           (1, 45, 'Haz enviado con éxito el {0}'),
           (1, 46, 'Información de Usuario'),
           (1, 47, 'Nombre'),
           (1, 48, 'Identificación'),
           (1, 49, 'Correo Electrónico'),
           (1, 50, 'Resultados del test'),
           (1, 51, 'Ver Resultados'),
           (1, 52, 'Ocultar Resultados'),
           (1, 53, 'Obteniendo resultados...'),
           (1, 54, 'No se pudieron obtener los resultados. Intenta de nuevo.'),
           (1, 55, 'No se encontraron resultados.'),
           (1, 56, 'Respuestas correctas:'),
           (1, 57, '{0} de {1}'),
           (1, 58, 'Preguntas omitidas:'),
           (1, 59, 'Porcentaje de aciertos:'),
           (1, 60, 'Tiempo total:'),
           (1, 61, '{0} segundos'),
           (1,
            62,
            'Tus resultados han sido guardados exitosamente. Estos datos serán utilizados exclusivamente con propósitos académicos y de investigación.'),
           (1, 63, 'Volver a Inicio'),
           (1, 64, 'Cargando resultados del test...'),
           (1, 65, '{0} - Analíticas'),
           (1, 66, 'Volver al Inicio'),
           (1, 67, 'Ver y exportar los resultados del test de los participantes.'),
           (1, 68, 'Lista de Participantes'),
           (1, 69, 'Resultados del Test'),
           (1, 70, 'Exportar CSV'),
           (1, 71, 'No hay resultados disponibles todavía'),
           (1, 72, 'Participante'),
           (1, 73, 'Grupo'),
           (1, 74, 'Fecha'),
           (1, 75, 'Tiempo (s)'),
           (1, 76, 'Correctas'),
           (1, 77, 'Respondidas'),
           (1, 78, 'Precisión'),
           (1, 79, 'Acciones'),
           (1, 80, 'ID: {0}'),
           (1, 81, 'Exportar resultados detallados'),
           (1, 82, 'Resumen'),
           (1, 83, 'Total Participantes'),
           (1, 84, 'Precisión Promedio'),
           (1, 85, 'Tiempo Promedio'),
           (1, 86, 'Total Preguntas Respondidas'),
           (1, 87, 'Error al cargar los resultados. Por favor, actualiza la página.'),
           (1, 88, 'No se encontraron resultados. Los usuarios deben completar el test para ver los resultados aquí.'),
           (1, 89, 'Error al exportar los datos como CSV.'),
           (1, 90, 'Error al exportar los datos del participante {0}.'),
           # TODO remove these eventually
           (1, 91, 'Usuario Anónimo'),
           (1, 92, 'Usuario con ID \'{0}\' no existe.'),
           (1, 93, 'Usuario con ID \'{0}\' ya existe.');

insert into home_info_card (config_version, `order`, title, description, icon_id, color)
    values (1,
            1,
            'Scientific purpose',
            'This test is part of academic research in the field of psychometrics.',
            2,
            880381),
           (1,
            2,
            'Voluntary participation',
            'Your participation is completely voluntary, and the data will be treated confidentially.',
            3,
            1673044);

insert into home_faq (config_version, `order`, question, answer)
    values (1,
            1,
            'How long does it take to complete the test?',
            'The time varies depending on the person, but it generally takes between 20 and 45 minutes.'),
           (1,
            2,
            'Can I pause the test and continue later?',
            'It is recommended to complete the test without interruptions to obtain more accurate results.'),
           (1,
            3,
            'How will my data be used?',
            'The data will be used solely for academic purposes and will be presented anonymously.'),
           (1,
            4,
            'Will I receive my results?',
            'At the end of the test, you will be provided with information about your performance.');

insert into form (config_version, type)
    values (1, 'pre'),
           (1, 'post');

insert into form_question (form_id,
                           `order`,
                           category,
                           text,
                           image_id,
                           type,
                           min,
                           max,
                           step,
                           other,
                           min_length,
                           max_length,
                           placeholder)
    values (1,
            1,
            'Demográfico',
            '¿Con cuál de las siguientes opciones te identificas?',
            default,
            'select-one',
            null,
            null,
            null,
            true,
            null,
            null,
            null),
           (1,
            2,
            'Demográfico',
            '¿Dónde estás viviendo actualmente en relación con tu lugar de origen?',
            default,
            'select-one',
            null,
            null,
            null,
            false,
            null,
            null,
            null),
           (1,
            3,
            'Demográfico',
            '¿Cuál es el nivel máximo de escolaridad de la madre?',
            default,
            'select-one',
            null,
            null,
            null,
            false,
            null,
            null,
            null),
           (1,
            4,
            'Demográfico',
            '¿En qué año académico te encuentras actualmente?',
            default,
            'select-one',
            null,
            null,
            null,
            false,
            null,
            null,
            null),
           (1,
            5,
            'Demográfico',
            '¿Qué esperas lograr en los próximos 5 años?',
            default,
            'select-one',
            null,
            null,
            null,
            false,
            null,
            null,
            null),
           (1, 6, 'GRIT-S', 'Los contratiempos me desaniman', default, 'slider', 1, 4, 1, null, null, null, null),
           (1, 7, 'GRIT-S', 'Soy muy trabajador/a', default, 'slider', 1, 4, 1, null, null, null, null),
           (1, 8, 'GRIT-S', 'Termino siempre todo lo que empiezo', default, 'slider', 1, 4, 1, null, null, null, null),
           (1,
            9,
            'GRIT-S',
            'Soy diligente (es decir, cuidadoso, activo y que ejecuta con celo y exactitud lo que esta a su cargo)',
            default,
            'slider',
            1,
            4,
            1,
            null,
            null,
            null,
            null),
           (1,
            10,
            'SRL',
            'Hago un plan antes de comenzar a hacer un trabajo escrito. Pienso lo que voy a hacer y lo que necesito para conseguirlo.\n',
            default,
            'slider',
            1,
            5,
            1,
            null,
            null,
            null,
            null),
           (1,
            11,
            'SRL',
            'Cuando estudio, intento comprender las materias, tomar apuntes, hacer resúmenes, resolver ejercicios, hacer preguntas sobre los contenidos.\n',
            default,
            'slider',
            1,
            5,
            1,
            null,
            null,
            null,
            null),
           (1,
            12,
            'SRL',
            'Después de terminar un examen parcial / final, lo reviso mentalmente para saber dónde tuve los aciertos y errores y, hacerme una idea de la nota que voy a tener.\n',
            default,
            'slider',
            1,
            5,
            1,
            null,
            null,
            null,
            null),
           (1,
            13,
            'TPB',
            'Depende de mí o no que tan bien me va en una actividad académica',
            default,
            'slider',
            1,
            7,
            1,
            null,
            null,
            null,
            null),
           (1,
            14,
            'TPB',
            'Si quiero puedo completar todas las tareas de las actividades académicas',
            default,
            'slider',
            1,
            7,
            1,
            null,
            null,
            null,
            null),
           (1,
            15,
            'TPB',
            'Depende de mí si mantengo al día mis actividades académicas',
            default,
            'slider',
            1,
            7,
            1,
            null,
            null,
            null,
            null),
           (2, 1, 'GRIT-S', 'Los contratiempos me desaniman', default, 'slider', 1, 4, 1, null, null, null, null),
           (2, 2, 'GRIT-S', 'Soy muy trabajador/a', default, 'slider', 1, 4, 1, null, null, null, null),
           (2, 3, 'GRIT-S', 'Termino siempre todo lo que empiezo', default, 'slider', 1, 4, 1, null, null, null, null),
           (2,
            4,
            'GRIT-S',
            'Soy diligente (es decir, cuidadoso, activo y que ejecuta con celo y exactitud lo que esta a su cargo)',
            default,
            'slider',
            1,
            4,
            1,
            null,
            null,
            null,
            null),
           (2,
            5,
            'SRL',
            'Hago un plan antes de comenzar a hacer un trabajo escrito. Pienso lo que voy a hacer y lo que necesito para conseguirlo.\n',
            default,
            'slider',
            1,
            5,
            1,
            null,
            null,
            null,
            null),
           (2,
            6,
            'SRL',
            'Cuando estudio, intento comprender las materias, tomar apuntes, hacer resúmenes, resolver ejercicios, hacer preguntas sobre los contenidos.\n',
            default,
            'slider',
            1,
            5,
            1,
            null,
            null,
            null,
            null),
           (2,
            7,
            'SRL',
            'Después de terminar un examen parcial / final, lo reviso mentalmente para saber dónde tuve los aciertos y errores y, hacerme una idea de la nota que voy a tener.\n',
            default,
            'slider',
            1,
            5,
            1,
            null,
            null,
            null,
            null),
           (2,
            8,
            'TPB',
            'Depende de mí o no que tan bien me va en una actividad académica',
            default,
            'slider',
            1,
            7,
            1,
            null,
            null,
            null,
            null),
           (2,
            9,
            'TPB',
            'Si quiero puedo completar todas las tareas de las actividades académicas',
            default,
            'slider',
            1,
            7,
            1,
            null,
            null,
            null,
            null),
           (2,
            10,
            'TPB',
            'Depende de mí si mantengo al día mis actividades académicas',
            default,
            'slider',
            1,
            7,
            1,
            null,
            null,
            null,
            null);

insert into form_question_option (form_question_id, `order`, text, image_id)
    values (1, 1, 'Femenino', default),
           (1, 2, 'Masculino', default),
           (1, 3, 'Prefiero no decirlo', default),
           (2, 1, 'Vivo en mi región de origen, con mi familia', default),
           (2, 2, 'Vivo en mi región de origen, de manera independiente', default),
           (2, 3, 'Vivo fuera de mi región de origen, con familiares', default),
           (2, 4, 'Vivo fuera de mi región de origen, independientemente', default),
           (3, 1, 'Educación básica incompleta', default),
           (3, 2, 'Educación básica completa', default),
           (3, 3, 'Educación media incompleta', default),
           (3, 4, 'Educación media completa', default),
           (3, 5, 'Educación técnica o profesional', default),
           (3, 6, 'Educación universitaria', default),
           (4, 1, 'Primero', default),
           (4, 2, 'Segundo', default),
           (4, 3, 'Tercero', default),
           (4, 4, 'Cuarto', default),
           (4, 5, 'Quinto', default),
           (4, 6, 'Sexto', default),
           (5, 1, 'Post grado profesional', default),
           (5, 2, 'Post grado académico', default),
           (5, 3, 'Otra formación profesional', default),
           (5, 4, 'Desarrollar experiencia laboral', default),
           (5, 5, 'Empezar mi propio negocio', default),
           (5, 6, 'Aún no lo sé', default);

insert into form_question_slider_label (form_question_id, number, label)
    values (6, 1, 'Totalmente en desacuerdo'),
           (6, 2, 'Algo en desacuerdo'),
           (6, 3, 'Algo de acuerdo'),
           (6, 4, 'Totalmente de acuerdo'),
           (7, 1, 'Totalmente en desacuerdo'),
           (7, 2, 'Algo en desacuerdo'),
           (7, 3, 'Algo de acuerdo'),
           (7, 4, 'Totalmente de acuerdo'),
           (8, 1, 'Totalmente en desacuerdo'),
           (8, 2, 'Algo en desacuerdo'),
           (8, 3, 'Algo de acuerdo'),
           (8, 4, 'Totalmente de acuerdo'),
           (9, 1, 'Totalmente en desacuerdo'),
           (9, 2, 'Algo en desacuerdo'),
           (9, 3, 'Algo de acuerdo'),
           (9, 4, 'Totalmente de acuerdo'),
           (10, 1, 'Totalmente en desacuerdo'),
           (10, 2, 'Algo en desacuerdo'),
           (10, 3, 'Ni acuerdo ni en desacuerdo'),
           (10, 4, 'Algo de acuerdo'),
           (10, 5, 'Totalmente de acuerdo'),
           (11, 1, 'Totalmente en desacuerdo'),
           (11, 2, 'Algo en desacuerdo'),
           (11, 3, 'Ni acuerdo ni en desacuerdo'),
           (11, 4, 'Algo de acuerdo'),
           (11, 5, 'Totalmente de acuerdo'),
           (12, 1, 'Totalmente en desacuerdo'),
           (12, 2, 'Algo en desacuerdo'),
           (12, 3, 'Ni acuerdo ni en desacuerdo'),
           (12, 4, 'Algo de acuerdo'),
           (12, 5, 'Totalmente de acuerdo'),
           (13, 1, 'Totalmente en desacuerdo'),
           (13, 7, 'Totalmente de acuerdo'),
           (14, 1, 'Totalmente en desacuerdo'),
           (14, 7, 'Totalmente de acuerdo'),
           (15, 1, 'Totalmente en desacuerdo'),
           (15, 7, 'Totalmente de acuerdo'),
           (16, 1, 'Totalmente en desacuerdo'),
           (16, 2, 'Algo en desacuerdo'),
           (16, 3, 'Algo de acuerdo'),
           (16, 4, 'Totalmente de acuerdo'),
           (17, 1, 'Totalmente en desacuerdo'),
           (17, 2, 'Algo en desacuerdo'),
           (17, 3, 'Algo de acuerdo'),
           (17, 4, 'Totalmente de acuerdo'),
           (18, 1, 'Totalmente en desacuerdo'),
           (18, 2, 'Algo en desacuerdo'),
           (18, 3, 'Algo de acuerdo'),
           (18, 4, 'Totalmente de acuerdo'),
           (19, 1, 'Totalmente en desacuerdo'),
           (19, 2, 'Algo en desacuerdo'),
           (19, 3, 'Algo de acuerdo'),
           (19, 4, 'Totalmente de acuerdo'),
           (20, 1, 'Totalmente en desacuerdo'),
           (20, 2, 'Algo en desacuerdo'),
           (20, 3, 'Ni acuerdo ni en desacuerdo'),
           (20, 4, 'Algo de acuerdo'),
           (20, 5, 'Totalmente de acuerdo'),
           (21, 1, 'Totalmente en desacuerdo'),
           (21, 2, 'Algo en desacuerdo'),
           (21, 3, 'Ni acuerdo ni en desacuerdo'),
           (21, 4, 'Algo de acuerdo'),
           (21, 5, 'Totalmente de acuerdo'),
           (22, 1, 'Totalmente en desacuerdo'),
           (22, 2, 'Algo en desacuerdo'),
           (22, 3, 'Ni acuerdo ni en desacuerdo'),
           (22, 4, 'Algo de acuerdo'),
           (22, 5, 'Totalmente de acuerdo'),
           (23, 1, 'Totalmente en desacuerdo'),
           (23, 7, 'Totalmente de acuerdo'),
           (24, 1, 'Totalmente en desacuerdo'),
           (24, 7, 'Totalmente de acuerdo'),
           (25, 1, 'Totalmente en desacuerdo'),
           (25, 7, 'Totalmente de acuerdo');

insert into test_group (config_version, label, probability, greeting)
    values (1,
            'saliente',
            0.5,
            '**¡Hola!**\n\nBienvenido/a. Por parte del equipo agradecemos tu participación en el experimento.\n\nAntes de comenzar una tarea desafiante, contar con una estrategia clara puede marcar una gran diferencia. Es por\nello que te sugerimos considerar lo siguiente antes de iniciar:\n\n1. **Planificar:** Aquí debes pensar brevemente que estrategias utilizaras para resolver la tarea propuesta.\n\n2. **Desarrollar:** Debes enfocarte en aplicar las estrategias previamente planificadas e ir monitoreando tu\ndesempeño.\n\n3. **Reflexionar:** Evalúa tu desempeño en base a las estrategias que propusiste y piensa que mejoras puedes hacer\npara aspirar al óptimo.\n\nRecuerda que ya has demostrado una gran capacidad para superar desafíos en el pasado, prueba de ello es que estas\nen una excelente universidad y en una carera desafiante. Esos éxitos pasados son poderosas herramientas para tus\nlogros futuros.\n\nTienes la preparación las habilidades y el potencial, cree en ti.\n\n**¡Tú puedes!**'),
           (1,
            'contextual',
            0.5,
            '**¡Hola!**\n\nBienvenido/a. Por parte del equipo agradecemos tu participación en el experimento. Su objetivo principal es\nexplorar como las personas enfrentan tareas desafiantes de resolución de problemas, el cual es parte de un estudio\nque busca medir persistencia.\n\nLa prueba que encontraras a continuación evalúa tu inteligencia fluida, la cual es una capacidad esencial para\nresolver problemas, a diferencia de la inteligencia cristalizada la cual depende de los conocimientos adquiridos\ncon el tiempo, la inteligencia fluida se considera independiente de la educación formal y experiencia previa.\n\nExisten distintos tipos de test diseñados para medir esta habilidad, basados en tareas visuales y no verbales, que\nevitan el uso de lenguaje o las matemáticas, lo que permite evaluar el razonamiento sin influencias culturales o\nlingüísticas.\n\nDurante la actividad te encontraras diversos desafíos, en donde debes señalar entre las diferentes opciones la que\nconsideres correcta.\n\n**¡Gracias por participar!**');

insert into test_protocol (config_version,
                           label,
                           group_id,
                           allow_previous_phase,
                           allow_previous_question,
                           allow_skip_question,
                           randomize_phases)
    values (1, 'saliente', 1, default, default, default, default),
           (1, 'contextual', 2, default, default, default, default);

insert into test_phase (protocol_id, `order`, randomize_questions)
    values (1, 1, default),
           (2, 1, default);

insert into test_question (phase_id, `order`, text, image_id, randomize_options)
    values (1, 1, default, 4, default),
           (1, 2, default, 5, default),
           (1, 3, default, 6, default),
           (1, 4, default, 7, default),
           (1, 5, default, 8, default),
           (1, 6, default, 9, default),
           (1, 7, default, 10, default),
           (1, 8, default, 11, default),
           (1, 9, default, 12, default),
           (1, 10, default, 13, default),
           (1, 11, default, 14, default),
           (1, 12, default, 15, default),
           (1, 13, default, 16, default),
           (2, 1, default, 17, default),
           (2, 2, default, 18, default),
           (2, 3, default, 19, default),
           (2, 4, default, 20, default),
           (2, 5, default, 21, default),
           (2, 6, default, 22, default),
           (2, 7, default, 23, default),
           (2, 8, default, 24, default),
           (2, 9, default, 25, default),
           (2, 10, default, 26, default),
           (2, 11, default, 27, default),
           (2, 12, default, 28, default),
           (2, 13, default, 29, default);

insert into test_option (question_id, `order`, text, image_id, correct)
    values (1, 1, '1', default, false),
           (1, 2, '2', default, false),
           (1, 3, '3', default, false),
           (1, 4, '4', default, false),
           (1, 5, '5', default, false),
           (1, 6, '6', default, true),
           (1, 7, '7', default, false),
           (1, 8, '8', default, false),
           (2, 1, '1', default, false),
           (2, 2, '2', default, false),
           (2, 3, '3', default, false),
           (2, 4, '4', default, true),
           (2, 5, '5', default, false),
           (2, 6, '6', default, false),
           (2, 7, '7', default, false),
           (2, 8, '8', default, false),
           (3, 1, '1', default, false),
           (3, 2, '2', default, false),
           (3, 3, '3', default, true),
           (3, 4, '4', default, false),
           (3, 5, '5', default, false),
           (3, 6, '6', default, false),
           (3, 7, '7', default, false),
           (3, 8, '8', default, false),
           (4, 1, '1', default, false),
           (4, 2, '2', default, false),
           (4, 3, '3', default, false),
           (4, 4, '4', default, false),
           (4, 5, '5', default, false),
           (4, 6, '6', default, false),
           (4, 7, '7', default, true),
           (4, 8, '8', default, false),
           (5, 1, '1', default, false),
           (5, 2, '2', default, false),
           (5, 3, '3', default, false),
           (5, 4, '4', default, false),
           (5, 5, '5', default, false),
           (5, 6, '6', default, true),
           (5, 7, '7', default, false),
           (5, 8, '8', default, false),
           (6, 1, '1', default, false),
           (6, 2, '2', default, false),
           (6, 3, '3', default, false),
           (6, 4, '4', default, false),
           (6, 5, '5', default, false),
           (6, 6, '6', default, false),
           (6, 7, '7', default, false),
           (6, 8, '8', default, true),
           (7, 1, '1', default, false),
           (7, 2, '2', default, true),
           (7, 3, '3', default, false),
           (7, 4, '4', default, false),
           (7, 5, '5', default, false),
           (7, 6, '6', default, false),
           (7, 7, '7', default, false),
           (7, 8, '8', default, false),
           (8, 1, '1', default, true),
           (8, 2, '2', default, false),
           (8, 3, '3', default, false),
           (8, 4, '4', default, false),
           (8, 5, '5', default, false),
           (8, 6, '6', default, false),
           (8, 7, '7', default, false),
           (8, 8, '8', default, false),
           (9, 1, '1', default, false),
           (9, 2, '2', default, false),
           (9, 3, '3', default, false),
           (9, 4, '4', default, false),
           (9, 5, '5', default, true),
           (9, 6, '6', default, false),
           (9, 7, '7', default, false),
           (9, 8, '8', default, false),
           (10, 1, '1', default, true),
           (10, 2, '2', default, false),
           (10, 3, '3', default, false),
           (10, 4, '4', default, false),
           (10, 5, '5', default, false),
           (10, 6, '6', default, false),
           (10, 7, '7', default, false),
           (10, 8, '8', default, false),
           (11, 1, '1', default, false),
           (11, 2, '2', default, false),
           (11, 3, '3', default, false),
           (11, 4, '4', default, false),
           (11, 5, '5', default, false),
           (11, 6, '6', default, true),
           (11, 7, '7', default, false),
           (11, 8, '8', default, false),
           (12, 1, '1', default, false),
           (12, 2, '2', default, false),
           (12, 3, '3', default, true),
           (12, 4, '4', default, false),
           (12, 5, '5', default, false),
           (12, 6, '6', default, false),
           (12, 7, '7', default, false),
           (12, 8, '8', default, false),
           (13, 1, '1', default, false),
           (13, 2, '2', default, true),
           (13, 3, '3', default, false),
           (13, 4, '4', default, false),
           (13, 5, '5', default, false),
           (13, 6, '6', default, false),
           (13, 7, '7', default, false),
           (13, 8, '8', default, false),
           (14, 1, '1', default, false),
           (14, 2, '2', default, false),
           (14, 3, '3', default, false),
           (14, 4, '4', default, false),
           (14, 5, '5', default, false),
           (14, 6, '6', default, true),
           (14, 7, '7', default, false),
           (14, 8, '8', default, false),
           (15, 1, '1', default, false),
           (15, 2, '2', default, false),
           (15, 3, '3', default, false),
           (15, 4, '4', default, true),
           (15, 5, '5', default, false),
           (15, 6, '6', default, false),
           (15, 7, '7', default, false),
           (15, 8, '8', default, false),
           (16, 1, '1', default, false),
           (16, 2, '2', default, false),
           (16, 3, '3', default, true),
           (16, 4, '4', default, false),
           (16, 5, '5', default, false),
           (16, 6, '6', default, false),
           (16, 7, '7', default, false),
           (16, 8, '8', default, false),
           (17, 1, '1', default, false),
           (17, 2, '2', default, false),
           (17, 3, '3', default, false),
           (17, 4, '4', default, false),
           (17, 5, '5', default, false),
           (17, 6, '6', default, false),
           (17, 7, '7', default, true),
           (17, 8, '8', default, false),
           (18, 1, '1', default, false),
           (18, 2, '2', default, false),
           (18, 3, '3', default, false),
           (18, 4, '4', default, false),
           (18, 5, '5', default, false),
           (18, 6, '6', default, true),
           (18, 7, '7', default, false),
           (18, 8, '8', default, false),
           (19, 1, '1', default, false),
           (19, 2, '2', default, false),
           (19, 3, '3', default, false),
           (19, 4, '4', default, false),
           (19, 5, '5', default, false),
           (19, 6, '6', default, false),
           (19, 7, '7', default, false),
           (19, 8, '8', default, true),
           (20, 1, '1', default, false),
           (20, 2, '2', default, true),
           (20, 3, '3', default, false),
           (20, 4, '4', default, false),
           (20, 5, '5', default, false),
           (20, 6, '6', default, false),
           (20, 7, '7', default, false),
           (20, 8, '8', default, false),
           (21, 1, '1', default, true),
           (21, 2, '2', default, false),
           (21, 3, '3', default, false),
           (21, 4, '4', default, false),
           (21, 5, '5', default, false),
           (21, 6, '6', default, false),
           (21, 7, '7', default, false),
           (21, 8, '8', default, false),
           (22, 1, '1', default, false),
           (22, 2, '2', default, false),
           (22, 3, '3', default, false),
           (22, 4, '4', default, false),
           (22, 5, '5', default, true),
           (22, 6, '6', default, false),
           (22, 7, '7', default, false),
           (22, 8, '8', default, false),
           (23, 1, '1', default, true),
           (23, 2, '2', default, false),
           (23, 3, '3', default, false),
           (23, 4, '4', default, false),
           (23, 5, '5', default, false),
           (23, 6, '6', default, false),
           (23, 7, '7', default, false),
           (23, 8, '8', default, false),
           (24, 1, '1', default, false),
           (24, 2, '2', default, false),
           (24, 3, '3', default, false),
           (24, 4, '4', default, false),
           (24, 5, '5', default, false),
           (24, 6, '6', default, true),
           (24, 7, '7', default, false),
           (24, 8, '8', default, false),
           (25, 1, '1', default, false),
           (25, 2, '2', default, false),
           (25, 3, '3', default, true),
           (25, 4, '4', default, false),
           (25, 5, '5', default, false),
           (25, 6, '6', default, false),
           (25, 7, '7', default, false),
           (25, 8, '8', default, false),
           (26, 1, '1', default, false),
           (26, 2, '2', default, true),
           (26, 3, '3', default, false),
           (26, 4, '4', default, false),
           (26, 5, '5', default, false),
           (26, 6, '6', default, false),
           (26, 7, '7', default, false),
           (26, 8, '8', default, false);
