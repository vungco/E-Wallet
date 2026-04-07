-- Bảng có thể đã bị Hibernate ddl-auto tạo với VARCHAR/TEXT nhỏ; đồng bộ với V2 (LONGTEXT).
ALTER TABLE event_outbox MODIFY COLUMN payload_json LONGTEXT NOT NULL;
