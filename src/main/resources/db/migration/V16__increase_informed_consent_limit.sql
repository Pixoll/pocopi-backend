-- Increase character limit for informed consent to 6000 characters
ALTER TABLE config MODIFY COLUMN informed_consent VARCHAR(6000) NOT NULL CHECK (informed_consent != '');
