ALTER TABLE notifications
    ADD COLUMN recipient_email VARCHAR(320) NULL AFTER user_id;
