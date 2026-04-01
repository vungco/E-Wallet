-- Auth: đăng nhập bằng email + mật khẩu; refresh token lưu dạng hash (SHA-256 hex), không lưu plaintext.

ALTER TABLE users
    ADD COLUMN email VARCHAR(255) NULL COMMENT 'nullable cho bản ghi cũ trước migration' AFTER name,
    ADD COLUMN password_hash VARCHAR(255) NULL AFTER email;

-- Một email một user (đăng ký mới); cho phép nhiều NULL (user cũ chưa có email).
CREATE UNIQUE INDEX uk_users_email ON users (email);

-- Refresh token: tra cứu theo hash khi client gửi refresh (đường dẫn nóng nhất → UNIQUE trên token_hash).
-- user_id: revoke theo user / liệt kê phiên.
-- expires_at: job dọn token hết hạn.
-- (user_id, revoked_at, expires_at): lọc token còn hiệu lực theo user (logout all, audit).
CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL COMMENT 'SHA-256 hex (64 ký tự) của refresh token plaintext',
    expires_at TIMESTAMP(0) NOT NULL,
    revoked_at TIMESTAMP(0) NULL,
    created_at TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    replaced_by_id BIGINT NULL COMMENT 'Luân phiên refresh: id bản ghi mới thay thế',
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token_hash (token_hash),
    KEY idx_refresh_tokens_user_id (user_id),
    KEY idx_refresh_tokens_expires_at (expires_at),
    KEY idx_refresh_tokens_user_active (user_id, revoked_at, expires_at),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_refresh_tokens_replaced_by FOREIGN KEY (replaced_by_id) REFERENCES refresh_tokens (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
