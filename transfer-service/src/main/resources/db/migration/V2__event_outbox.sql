CREATE TABLE event_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transfer_id BIGINT NOT NULL,
    topic VARCHAR(128) NOT NULL,
    partition_key VARCHAR(64) NOT NULL,
    payload_json LONGTEXT NOT NULL,
    published_at TIMESTAMP(0) NULL,
    created_at TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_outbox_unpublished (published_at),
    CONSTRAINT fk_outbox_transfer FOREIGN KEY (transfer_id) REFERENCES transfers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
