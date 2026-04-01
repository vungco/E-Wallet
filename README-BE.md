# Mini E-Wallet — Chiến lược Backend (BE)

**Đã chốt:** kiến trúc **microservice** — **mỗi module (bounded context) = một Spring Boot service deploy riêng + một database MySQL riêng** (schema/logical DB độc lập; dev có thể dùng một instance MySQL chứa nhiều database).

Stack: **Java 17+**, **Spring Boot 3.x**, **MySQL 8**, **Kafka** (sự kiện sau khi nghiệp vụ tiền đã nhất quán trong từng service + outbox).

---

## 1. Nguyên tắc kiến trúc

| Nguyên tắc | Nội dung |
|------------|----------|
| **Một DB / một service** | Không chia sẻ bảng giữa service qua DB chung; chỉ giao tiếp qua **API**, **message**, hoặc **sự kiện**. |
| **Số dư & ví** | **wallet-registry** là nơi duy nhất cập nhật `balance` (trong DB của nó). |
| **Lệnh chuyển & lịch sử nghiệp vụ** | **transfer-service** giữ bản ghi giao dịch / trạng thái saga / idempotency trong DB của nó. |
| **Không có transaction SQL xuyên DB** | Nhất quán liên service dùng **Saga (orchestration)** + **bù trừ (compensation)** + **idempotency key** trên mỗi bước gọi wallet. |
| **Kafka** | Không thay thế DB; publish sau khi trạng thái local + **transactional outbox** (khuyến nghị) để không “mất event” hoặc publish khi chưa commit. |
| **Gateway** | Client (FE) chỉ gọi **ewallet-gateway** (hoặc BFF); không gọi thẳng từng microservice ra production. |

---

## 2. Danh sách service và database (chốt)

Mỗi dòng: **tên service** ↔ **tên database** ↔ **trách nhiệm**.

| Service (container / artifact) | Database (MySQL) | Dữ liệu chính |
|-------------------------------|------------------|---------------|
| **`ewallet-gateway`** | *không* (stateless) | Chỉ routing, (sau) auth, rate limit. |
| **`wallet-registry`** | **`wallet_db`** | `users`, `wallets` (`balance`, `version`), mapping user↔ví. |
| **`transfer-service`** | **`transfer_db`** | Yêu cầu chuyển tiền, trạng thái saga, **idempotency** theo `requestId`, **outbox** Kafka, projection lịch sử đọc được từ phía transfer (hoặc query qua API). |
| **`audit-worker`** | **`audit_db`** | Bản ghi audit **append-only** (nhận từ Kafka). |
| **`notification-worker`** | **`notification_db`** *(tuỳ chọn)* | Log gửi notify đã xử lý / idempotency consumer để tránh gửi trùng. |

**Ghi chú dev:** Trên Docker Compose có thể **một container MySQL** tạo sẵn nhiều `CREATE DATABASE wallet_db`, `transfer_db`, … — vẫn đúng tinh thần “một DB riêng / service”; production có thể tách instance.

---

## 3. API công khai vs nội bộ

### 3.1. Qua gateway (FE / bên ngoài)

- `POST /api/v1/transfers` — body: `fromUserId`, `toUserId`, `amount`, `requestId` → **forward tới transfer-service**.
- `GET /api/v1/wallets/...`, `GET /api/v1/transactions/...` — route tới **wallet-registry** hoặc **transfer-service** tùy contract (gateway map path).

### 3.2. Nội bộ (chỉ mạng nội bộ / mTLS)

**transfer-service** gọi **wallet-registry** để thực hiện từng bước tiền (mỗi bước **một transaction** trong `wallet_db`):

- Ví dụ: `POST /internal/v1/wallets/{walletId}/debit` — kèm header **`Idempotency-Key`** (hoặc body key trùng với bước saga).
- `POST /internal/v1/wallets/{walletId}/credit` — tương tự.

Trong **wallet-registry**: với mỗi lệnh debit/credit: `SELECT … FOR UPDATE` đúng một ví + kiểm tra số dư (debit) + cập nhật `version`.

---

## 4. Luồng chuyển tiền (Saga — orchestration)

**Orchestrator:** **transfer-service**.

1. **Idempotency:** Nếu `requestId` đã có trong `transfer_db` với trạng thái terminal → trả lại kết quả cũ (không gọi wallet lần nữa).
2. Ghi nhận yêu cầu `PENDING` trong `transfer_db`.
3. **Debit** ví gửi qua API wallet (idempotent theo key bước 1).
4. **Credit** ví nhận qua API wallet (idempotent theo key bước 2).
5. Cập nhật trạng thái `SUCCESS`, ghi bản ghi lịch sử immutable trong `transfer_db`.
6. **Outbox** trong cùng transaction local → publish Kafka (worker không sửa balance).

**Lỗi / bù:**

- Nếu debit thành công mà credit thất bại → **compensate**: credit lại ví gửi (hoặc debit ngược có kiểm soát) với idempotency tương ứng; ghi `FAILED` + lý do.

**Thứ tự khóa:** Tránh deadlock khi nhiều giao dịch: quy ước luôn gọi debit/credit theo **thứ tự `walletId` tăng dần** khi cần chạm hai ví trong cùng saga (chi tiết implement trong transfer-service).

---

## 5. Kafka

| Topic (ví dụ) | Producer | Consumer |
|---------------|----------|----------|
| `wallet.transfer.completed` | **transfer-service** (sau commit + outbox) | **audit-worker**, **notification-worker** |

Partition key: `walletId` hoặc `fromUserId` để tuần tự hoá theo ví khi cần.

Consumer **không** cập nhật `balance` trong `wallet_db`.

---

## 6. Cấu trúc repo (gợi ý)

**Monorepo** `ewallet-backend`:

```
ewallet-backend/
├── ewallet-gateway/           # Spring Cloud Gateway hoặc Spring MVC proxy
├── wallet-registry/           # + Flyway cho wallet_db
├── transfer-service/          # + Flyway cho transfer_db
├── audit-worker/
├── notification-worker/
└── docker-compose.yml         # gateway + 5 service + kafka + mysql (multi-db)
```

Mỗi thư mục con (trừ `docker-compose.yml`) là **một project build độc lập** (một JAR / một image). **OpenAPI / tài liệu API** (nếu cần) đặt **trong từng service** (`src/main/resources/static/openapi.yaml` hoặc tương đương) — **không** bắt buộc thêm folder hợp đồng riêng.

---

## 7. Bản tóm tắt giai đoạn (G0 → G5)

| Giai đoạn | Việc làm | Tiêu chí xác thực |
|-----------|----------|-------------------|
| **G0** | Docker Compose: MySQL (nhiều DB), Kafka, network nội bộ. | `docker compose up` đủ dependency. |
| **G1** | **wallet-registry** + `wallet_db`. | Debit/credit an toàn + idempotency. |
| **G2** | **transfer-service** + `transfer_db` + saga gọi wallet. | Chuyển tiền đúng; `requestId` trùng an toàn; compensate. |
| **G3** | **ewallet-gateway** route tới các service. | Chỉ gọi qua gateway. |
| **G4** | Outbox + Kafka trong **transfer-service**. | Event sau commit; không publish khi rollback. |
| **G5** | **audit-worker**, **notification-worker**. | Consumer không sửa balance. |

**Thứ tự phụ thuộc:** `G0` → **`wallet-registry`** (G1) → **`transfer-service`** (G2) → **`ewallet-gateway`** (G3) → bổ sung **outbox/Kafka** trên transfer (G4) → **workers** (G5).

Chi tiết từng bước: **mục 8**.

---

## 8. Chiến lược thực thi step-by-step theo từng service

### 8.0. G0 — Hạ tầng chung (không nghiệp vụ)

1. Tạo `docker-compose.yml`: **MySQL** (một instance, nhiều `CREATE DATABASE`: `wallet_db`, `transfer_db`, `audit_db`, `notification_db`).
2. Thêm **Kafka** (KRaft hoặc Zookeeper tùy image team chọn); expose port dev.
3. Định nghĩa **mạng Docker** chung để các service gọi nhau bằng **tên container** (ví dụ `http://wallet-registry:8081`).
4. (Tuỳ chọn) file `.env` chung: port public gateway, JDBC URL từng service.
5. **Xác thực:** `docker compose up`, kết nối được MySQL từ máy host; Kafka có topic tạo thử được.

---

### 8.1. `wallet-registry` (làm trước — transfer phụ thuộc)

| Bước | Việc làm cụ thể |
|------|-----------------|
| 1 | Khởi tạo Spring Boot project, datasource **chỉ** `wallet_db`, **Flyway/Liquibase** migration. |
| 2 | Tạo bảng tối thiểu: `users`, `wallets` (`user_id`, `balance`, `version`). Seed data dev (vài user + ví + số dư ban đầu). |
| 3 | API **đọc** công khai hoặc nội bộ: `GET` ví theo `userId` / `walletId` (phục vụ sau này gateway/FE). |
| 4 | API **internal** `POST .../debit` và `POST .../credit` (hoặc một resource thống nhất): trong **một** `@Transactional`, `SELECT … FOR UPDATE` đúng một `wallet_id`, kiểm tra số dư (debit), cập nhật `balance` và `version`. |
| 5 | **Idempotency:** bảng `wallet_idempotency` (hoặc tương đương) lưu `(idempotency_key, bước, kết quả)` trong `wallet_db`; cùng key → trả cùng kết quả, không cộng/trừ lại. |
| 6 | **Health** Actuator `/actuator/health`; cấu hình port (ví dụ 8081). |
| 7 | **Test:** JUnit + Testcontainers MySQL — đồng thời nhiều request debit cùng ví; gọi trùng `Idempotency-Key` không trừ hai lần. |

**Dừng lại khi:** có thể gọi HTTP từ Postman (hoặc từ integration test) debit/credit đúng và an toàn.

---

### 8.2. `transfer-service` (sau wallet — orchestrator Saga)

| Bước | Việc làm cụ thể |
|------|-----------------|
| 1 | Spring Boot + datasource **chỉ** `transfer_db` + migration. |
| 2 | Bảng: yêu cầu chuyển (`request_id` UNIQUE), trạng thái (`PENDING` / `SUCCESS` / `FAILED`), `from_user_id`, `to_user_id`, `amount`, thời gian; (tuỳ chọn) bảng `saga_step` để debug. |
| 3 | Cấu hình **HTTP client** (RestClient/WebClient) tới URL **wallet-registry** (biến môi trường `WALLET_REGISTRY_BASE_URL`). |
| 4 | Map `userId` → `walletId`: gọi API wallet đã có ở 8.1 (hoặc embed id ví trong bảng transfer nếu FE gửi `walletId`). |
| 5 | **Luồng saga:** (a) kiểm tra `requestId` trong `transfer_db` → idempotent; (b) ghi `PENDING`; (c) resolve hai `walletId`; (d) **debit** sender với idempotency key dạng `{requestId}-debit`; (e) **credit** receiver `{requestId}-credit`; (f) nếu lỗi sau debit → **compensate** (credit lại sender cùng key bù); (g) cập nhật `SUCCESS` / `FAILED`. |
| 6 | API **public:** `POST /api/v1/transfers`, `GET /api/v1/transactions` (hoặc tương đương) — port riêng (ví dụ 8082). |
| 7 | **Test:** integration gọi wallet thật (Testcontainers cả hai DB hoặc mock wallet); case trùng `requestId`; case credit fail → balance khôi phục. |

**Giai đoạn này chưa cần Kafka** — chỉ cần chuyển tiền đúng end-to-end khi gọi thẳng `transfer-service` (hoặc sau gateway ở 8.3).

---

### 8.3. `ewallet-gateway`

| Bước | Việc làm cụ thể |
|------|-----------------|
| 1 | Spring Cloud Gateway (YAML/Java config) **hoặc** Spring MVC + `RestTemplate`/`RestClient` reverse proxy đơn giản. |
| 2 | Route ví dụ: `/api/v1/transfers/**` → `transfer-service`; `/api/v1/wallets/**` → `wallet-registry`; `/api/v1/transactions/**` → `transfer-service` (theo contract). |
| 3 | **StripPrefix** / giữ nguyên path tùy cách các service expose (thống nhất một kiểu). |
| 4 | Port public duy nhất (ví dụ 8080); các service backend chỉ bind mạng nội bộ. |
| 5 | **Xác thực:** toàn bộ luồng Postman/FE chỉ gọi `localhost:8080`. |

---

### 8.4. `transfer-service` — bổ sung G4: Outbox + Kafka

| Bước | Việc làm cụ thể |
|------|-----------------|
| 1 | Bảng `outbox` trong **cùng** `transfer_db`: `id`, `payload`, `topic`, `created_at`, `published_at` (null = chưa gửi). |
| 2 | Trong transaction khi chuyển sang `SUCCESS`: insert một dòng outbox (cùng commit với bản ghi giao dịch). |
| 3 | **Publisher** (scheduled hoặc `@TransactionalEventListener`): đọc outbox chưa publish → gửi Kafka → đánh dấu `published_at` (hoặc xóa sau khi ack tùy chiến lược). |
| 4 | Tạo topic **`wallet.transfer.completed`**, định nghĩa JSON payload cố định (document trong code hoặc README service). |
| 5 | **Xác thực:** rollback giao dịch → không có message; thành công → có đúng một event (hoặc semantics at-least-once + consumer idempotent). |

---

### 8.5. `audit-worker`

| Bước | Việc làm cụ thể |
|------|-----------------|
| 1 | Spring Boot + `@KafkaListener` topic `wallet.transfer.completed`. |
| 2 | Datasource **`audit_db`**; bảng append-only (ví dụ `audit_events`: `id`, `payload`, `received_at`, `transaction_id`). |
| 3 | Ghi **insert** một dòng mỗi message; không UPDATE balance. |
| 4 | **Xác thực:** sau một transfer thành công, có dòng trong `audit_db`; consumer restart vẫn an toàn nếu dùng idempotency `transaction_id` (unique). |

---

### 8.6. `notification-worker`

| Bước | Việc làm cụ thể |
|------|-----------------|
| 1 | `@KafkaListener` cùng topic (hoặc topic riêng sau này). |
| 2 | Logic “gửi thông báo” (log console / stub email trong dev). |
| 3 | (Tuỳ chọn) **`notification_db`**: bảng `processed_events` (`event_id` UNIQUE) để không xử lý trùng khi Kafka at-least-once. |
| 4 | **Xác thực:** không gọi wallet; không đổi `balance`. |

---

## 9. Kiểm thử bắt buộc (toàn hệ thống)

- **Concurrency** trên cùng ví (qua wallet internal API và qua saga).
- **Duplicate `requestId`** ở transfer.
- **Saga:** mô phỏng lỗi giữa debit và credit → số dư khôi phục đúng sau compensate.
- **Edge:** chuyển cho chính mình, `amount <= 0` — từ chối rõ ràng.

---

## 10. Checklist chốt kiến trúc (self-review)

- [ ] Mỗi service (trừ gateway) có **đúng một database** tên riêng, migration riêng.
- [ ] **wallet-registry** là chỗ duy nhất đổi `balance`.
- [ ] **transfer-service** không join SQL sang `wallet_db`; chỉ gọi HTTP (hoặc gRPC sau này).
- [ ] Có **Saga + idempotency** cho từng bước debit/credit.
- [ ] Kafka chỉ sau **commit** local + **outbox** (hoặc tương đương an toàn).
- [ ] FE chỉ vào **gateway** (khi G3 xong).

---

## 11. Liên kết

- Tổng quan: [README.md](./README.md)  
- Frontend: [README-FE.md](./README-FE.md) — `NUXT_PUBLIC_API_BASE` trỏ **gateway**.
