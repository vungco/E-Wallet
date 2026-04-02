-- Ghi nhận kết quả đã xử lý theo Idempotency-Key (saga / transfer-service).
CREATE TABLE wallet_idempotency (
    id BIGINT NOT NULL AUTO_INCREMENT,
    idempotency_key VARCHAR(128) NOT NULL,
    wallet_id BIGINT NOT NULL,
    operation VARCHAR(16) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    balance_after DECIMAL(19, 4) NOT NULL,
    wallet_version_snapshot BIGINT NOT NULL,
    created_at TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wallet_idempotency_key (idempotency_key),
    KEY idx_wallet_idempotency_wallet (wallet_id),
    CONSTRAINT fk_wallet_idempotency_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
