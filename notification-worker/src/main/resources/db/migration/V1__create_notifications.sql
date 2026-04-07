CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    transfer_status VARCHAR(32) NOT NULL,
    read_flag BIT(1) NOT NULL DEFAULT 0,
    transaction_id BIGINT,
    amount DECIMAL(19, 4) NOT NULL,
    counterpart_user_id BIGINT NOT NULL,
    user_role VARCHAR(16) NOT NULL,
    email_sent BIT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_notifications_idempotency (idempotency_key),
    KEY idx_notifications_user_read (user_id, read_flag),
    KEY idx_notifications_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
