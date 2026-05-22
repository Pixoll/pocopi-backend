-- Increase character limit for informed consent to 10000 characters
ALTER TABLE config MODIFY COLUMN informed_consent VARCHAR(10000) NOT NULL CHECK (informed_consent != '');
