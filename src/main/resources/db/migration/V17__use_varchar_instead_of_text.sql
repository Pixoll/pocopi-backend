alter table test_question
    modify column text varchar(10000) default null check (text is null or text != '');

alter table test_option
    modify column text varchar(10000) default null check (text is null or text != '');

alter table test_group
    modify column greeting varchar(11000) default null check (greeting is null or greeting != '');
