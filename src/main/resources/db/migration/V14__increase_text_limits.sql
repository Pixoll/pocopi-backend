-- Increase character limits for test questions, options and greetings to support longer texts and code blocks.
ALTER TABLE test_question MODIFY COLUMN text TEXT DEFAULT NULL CHECK (text is null or text != '');
ALTER TABLE test_option MODIFY COLUMN text TEXT DEFAULT NULL CHECK (text is null or text != '');
ALTER TABLE test_group MODIFY COLUMN greeting TEXT DEFAULT NULL CHECK (greeting is null or greeting != '');
