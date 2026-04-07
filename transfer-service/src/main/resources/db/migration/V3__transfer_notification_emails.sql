ALTER TABLE transfers
    ADD COLUMN from_user_email VARCHAR(320) NULL AFTER to_user_id,
    ADD COLUMN to_user_email VARCHAR(320) NULL AFTER from_user_email;
