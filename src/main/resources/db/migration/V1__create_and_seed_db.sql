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

create trigger before_insert_form_question
    before insert
    on form_question
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

create trigger before_update_form_question
    before update
    on form_question
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

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

create table form_question_slider_label (
    id               int4 unsigned primary key not null auto_increment,
    form_question_id int4 unsigned             not null,
    number           int2 unsigned             not null,
    label            varchar(50)               not null check (label != ''),
    unique (form_question_id, number),
    foreign key (form_question_id) references form_question (id) on delete cascade
);

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

create trigger before_insert_test_question
    before insert
    on test_question
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

create trigger before_update_test_question
    before update
    on test_question
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

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

create trigger before_insert_test_option
    before insert
    on test_option
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

create trigger before_update_test_option
    before update
    on test_option
    for each row
begin
    if (new.text is null and new.image_id is null) then
        signal sqlstate '45000' set message_text = 'text and image cannot be null at the same time';
    end if;
end;

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

create trigger before_update_user_form_submission
    before update
    on user_form_submission
    for each row
begin
    signal sqlstate '45000' set message_text = 'user_form_submission\'s rows cannot be updated';
end;

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

create trigger before_update_user_form_answer
    before update
    on user_form_answer
    for each row
begin
    signal sqlstate '45000' set message_text = 'user_form_answer\'s rows cannot be updated';
end;

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

create trigger before_update_user_test_question_log
    before update
    on user_test_question_log
    for each row
begin
    signal sqlstate '45000' set message_text = 'user_test_question_log\'s rows cannot be updated';
end;

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

create trigger before_update_user_test_option_log
    before update
    on user_test_option_log
    for each row
begin
    signal sqlstate '45000' set message_text = 'user_test_option_log\'s rows cannot be updated';
end;

insert into config (title, subtitle, description, informed_consent, anonymous)
    values ('PoCoPI Test',
            'A subtitle for your test.',
            'Write a description for your test here. **You can even use Markdown!**',
            'Write the informed consent form here. **You can also use Markdown here!**',
            true);

insert into translation_key (`key`, description, arguments)
    values ('home.dashboardButtonHint',
            'Tooltip to show when hovering over the dashboard button in the home page.',
            json_array()),
           ('home.themeLight', 'Label for the light theme.', json_array()),
           ('home.themeDark', 'Label for the dark theme.', json_array()),
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
            json_array('The username/id of the participant.'));

insert into translation_value (config_version, key_id, value)
    values (1, 1, 'Administration Panel'),
           (1, 2, 'light'),
           (1, 2, 'dark'),
           (1, 4, 'Switch to {0} mode'),
           (1, 5, 'Participant: {0} ({1})'),
           (1, 6, 'About this test'),
           (1, 7, 'Informed Consent'),
           (1, 8, 'I have read and accept the informed consent'),
           (1, 9, 'Start Test'),
           (1, 10, 'Register'),
           (1, 11, 'Frequently Asked Questions'),
           (1, 12, 'Please enter a valid {0}.'),
           (1, 13, 'Participant Information'),
           (1, 14, 'Your data will be treated confidentially and will be used exclusively for academic purposes.'),
           (1, 15, 'Full Name'),
           (1, 16, 'Identification Number'),
           (1, 17, 'Age'),
           (1, 18, 'Email Address'),
           (1, 19, 'Cancel'),
           (1, 20, 'Save Information'),
           (1, 21, 'Please specify...'),
           (1, 22, 'You must answer all questions.'),
           (1, 23, 'Submit answers'),
           (1, 24, 'Submitting answers...'),
           (1, 25, 'Pre-Test Form'),
           (1, 26, 'Post-Test Form'),
           (1, 27, 'Test Information'),
           (1, 28, 'Start Test'),
           (1, 29, 'Progress:'),
           (1, 30, 'Phase {0}/{1} - Question {2}/{3}'),
           (1, 31, 'Previous Phase'),
           (1, 32, 'Previous'),
           (1, 33, 'Next'),
           (1, 34, 'Back to summary'),
           (1, 35, 'Phase {0} Summary'),
           (1, 36, 'Test Summary'),
           (1, 37, 'Continue to next phase'),
           (1, 38, 'Finish test'),
           (1, 39, 'Phase'),
           (1, 40, 'Question'),
           (1, 41, 'Status'),
           (1, 42, 'Answered'),
           (1, 43, 'Unanswered'),
           (1, 44, 'Test Completed!'),
           (1, 45, 'Successfully Submitted'),
           (1, 46, 'Thank you very much {0} for your participation'),
           (1, 47, 'You have successfully submitted the {0}'),
           (1, 48, 'User Information'),
           (1, 49, 'Name'),
           (1, 50, 'Identification'),
           (1, 51, 'Email Address'),
           (1, 52, 'Test results'),
           (1, 53, 'Show Results'),
           (1, 54, 'Hide Results'),
           (1, 55, 'Fetching results...'),
           (1, 56, 'Could not retrieve results. Please try again.'),
           (1, 57, 'No results found.'),
           (1, 58, 'Correct answers:'),
           (1, 59, '{0} of {1}'),
           (1, 60, 'Skipped questions:'),
           (1, 61, 'Success rate:'),
           (1, 62, 'Total time:'),
           (1, 63, '{0} seconds'),
           (1,
            64,
            'Your results have been saved successfully. This data will be used exclusively for academic and research purposes.'),
           (1, 65, 'Back to Home'),
           (1, 66, 'Loading test results...'),
           (1, 67, '{0} - Analytics'),
           (1, 68, 'Back to Home'),
           (1, 69, 'View and export test results from participants.'),
           (1, 70, 'Participant List'),
           (1, 71, 'Test Results'),
           (1, 72, 'Export CSV'),
           (1, 73, 'No results available yet'),
           (1, 74, 'Participant'),
           (1, 75, 'Group'),
           (1, 76, 'Date'),
           (1, 77, 'Time (s)'),
           (1, 78, 'Correct'),
           (1, 79, 'Answered'),
           (1, 80, 'Accuracy'),
           (1, 81, 'Actions'),
           (1, 82, 'ID: {0}'),
           (1, 83, 'Export detailed results'),
           (1, 84, 'Summary'),
           (1, 85, 'Total Participants'),
           (1, 86, 'Average Accuracy'),
           (1, 87, 'Average Time'),
           (1, 88, 'Total Questions Answered'),
           (1, 89, 'Error loading results. Please refresh the page.'),
           (1, 90, 'No results found. Users must complete the test to see results here.'),
           (1, 91, 'Error exporting data as CSV.'),
           (1, 92, 'Error exporting data for participant {0}.');
