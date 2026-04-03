-- Append-only: audit-worker chỉ INSERT, không UPDATE/DELETE nghiệp vụ.
CREATE TABLE wallet_transfer_completed_audit (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL COMMENT 'transfer.id từ transfer_db — idempotent',
    request_id VARCHAR(128) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    occurred_at VARCHAR(64) NOT NULL,
    payload_json LONGTEXT NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_transaction_id (transaction_id),
    KEY idx_request_id (request_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
