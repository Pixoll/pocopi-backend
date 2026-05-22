update translation_value tv
set tv.value = 'Continue previous attempt'
    where tv.config_version = 1
      and tv.key_id = (
        select tk.id from translation_key tk where tk.`key` = 'test.continueTest'
        );

update translation_value tv
set tv.value = 'Discard and start a new one'
    where tv.config_version = 1
      and tv.key_id = (
        select tk.id
            from translation_key tk
            where tk.`key` = 'test.discardTest'
        );

update translation_value tv
set tv.value = 'Pre-Test Form'
    where tv.config_version = 1
      and tv.key_id = (
        select tk.id from translation_key tk where tk.`key` = 'preTestForm.title'
        );

update translation_value tv
set tv.value = 'Submit form'
    where tv.config_version = 1
      and tv.key_id = (
        select tk.id from translation_key tk where tk.`key` = 'preTestForm.submitForm'
        );

update translation_value tv
set tv.value = 'Post-Test Form'
    where tv.config_version = 1
      and tv.key_id = (
        select tk.id from translation_key tk where tk.`key` = 'postTestForm.title'
        );

update translation_value tv
set tv.value = 'Submit form'
    where tv.config_version = 1
      and tv.key_id = (
        select tk.id from translation_key tk where tk.`key` = 'postTestForm.submitForm'
        );
