-- Register missing translation keys required by payload and frontend
insert into translation_key (`key`, description, arguments)
values ('test.continueTest', 'Label to continue a previous test attempt.', json_array()),
       ('test.discardTest', 'Label to discard the previous attempt and start a new one.', json_array()),
       ('preTestForm.title', 'Title for the pre-test form page in some payloads.', json_array()),
       ('preTestForm.submitForm', 'Label for pre-test form submission.', json_array()),
       ('postTestForm.title', 'Title for the post-test form page in some payloads.', json_array()),
       ('postTestForm.submitForm', 'Label for post-test form submission.', json_array());

-- Seed values for these keys in existing configs
insert into translation_value (config_version, key_id, value)
select c.version, tk.id, 'Continuar intento anterior'
from config c cross join translation_key tk
where tk.`key` = 'test.continueTest';

insert into translation_value (config_version, key_id, value)
select c.version, tk.id, 'Descartar y comenzar uno nuevo'
from config c cross join translation_key tk
where tk.`key` = 'test.discardTest';

insert into translation_value (config_version, key_id, value)
select c.version, tk.id, 'Cuestionario Inicial'
from config c cross join translation_key tk
where tk.`key` = 'preTestForm.title';

insert into translation_value (config_version, key_id, value)
select c.version, tk.id, 'Enviar Respuestas'
from config c cross join translation_key tk
where tk.`key` = 'preTestForm.submitForm';

insert into translation_value (config_version, key_id, value)
select c.version, tk.id, 'Cuestionario Final'
from config c cross join translation_key tk
where tk.`key` = 'postTestForm.title';

insert into translation_value (config_version, key_id, value)
select c.version, tk.id, 'Finalizar Sesión'
from config c cross join translation_key tk
where tk.`key` = 'postTestForm.submitForm';

-- Increase character limit for form question text to 400
ALTER TABLE form_question MODIFY COLUMN text VARCHAR(400) DEFAULT NULL CHECK (text is null or text != '');
