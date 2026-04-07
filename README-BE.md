# Mini E-Wallet — Backend (BE)

Tài liệu mô tả **bài toán**, **kiến trúc**, **phụ thuộc giữa service** và **việc cần làm từng service**.  
**Công nghệ đã chốt:** **gRPC** (giao tiếp đồng bộ / lấy dữ liệu giữa service), **Kafka** (message, xếp hàng, giảm tốn tài nguyên khi chờ lock DB), **WebSocket** (`ws-gateway` — bắn real-time cho user biết kết quả / cập nhật liên quan số dư).

**Stack:** Java 17+, Spring Boot, MySQL 8, Kafka, gRPC (protobuf).

---

## 1. Bài toán đặt ra

| Nhu cầu | Thách thức |
|---------|------------|
| **Ví điện tử** | Mỗi user có ví; **số dư** phải **nhất quán**, không double-spend. |
| **Chuyển tiền** | Thao tác gồm nhiều bước (trừ người gửi, cộng người nhận); cần **saga**, rollback khi lỗi. |
| **Chịu tải** | Ghi **cùng một ví** trên DB dùng **lock dòng** (`SELECT … FOR UPDATE`). Nếu **quá nhiều request đồng thời** đổ thẳng vào DB, **thread và connection** chờ lock → **tốn tài nguyên** toàn hệ thống. |
| **Trải nghiệm** | User không nên **chờ HTTP dài** trong lúc DB khóa ví; cần **chấp nhận lệnh nhanh** và **thông báo kết quả sau** (real-time). |
| **Quan sát & hậu xử lý** | Ghi **audit**; có thể **notify** — không làm chậm path chuyển tiền. |

**Ý tưởng xử lý tải:** không “đập” hàng loạt ghi ví **đồng bộ từ edge**. Dùng **Kafka** làm **hàng đợi** trước worker; **partition key theo ví nguồn** (`fromWalletId`) để **tuần tự hóa** lệnh cùng ví, giảm tranh lock hỗn loạn. **Kết quả** đẩy qua **Kafka → ws-gateway → WebSocket** để user biết **đã xong / lỗi** (và có thể **GET lại số dư** sau khi thành công).

---

## 2. Giải pháp: microservice + nhiều database

**Nguyên tắc:** **một bounded context ≈ một Spring Boot service + một MySQL riêng** — **không join SQL xuyên service**.

| Cách làm | Lý do |
|----------|--------|
| **Tách DB** | Giới hạn blast radius; mỗi team chủ một schema. |
| **gRPC nội bộ** | Gọi **đồng bộ** rõ contract (protobuf): debit/credit ví, query cần thiết. **REST** chỉ **client ↔ `ewallet-gateway`**. |
| **Kafka** | **Không** thay DB: tiền vẫn commit trong transaction. Kafka dùng để **xếp hàng lệnh chuyển**, **outbox** sau commit, **`transfer.result`** cho WebSocket. |
| **202 + worker** | HTTP nhận lệnh, trả **ngay** `202` + `requestId`; worker **consumer** mới gọi **gRPC → wallet-registry** — tránh ôm connection từ phía user trong lúc chờ lock. |

**Nguồn sự thật số dư:** chỉ **`wallet-registry`** / `wallet_db`. Các service khác **không** sửa `balance` trực tiếp.

---

## 3. Tổng quan phụ thuộc giữa các service

```text
                         ┌──────────────────────┐
                         │   ewallet-gateway    │  ← REST/HTTPS (FE)
                         └──────────┬───────────┘
                                    │ gRPC
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
          ┌──────────────────┐            ┌──────────────────┐
          │ wallet-registry  │◄── gRPC ───│ transfer-service │
          │   (wallet_db)    │   (saga)   │  (transfer_db)   │
          └──────────────────┘            └────────┬─────────┘
                                                   │ produce
                                                   ▼
                                          ┌───────────────┐
                                          │     Kafka     │
                                          └───────┬───────┘
                    ┌────────────────────────────┼────────────────────────────┐
                    ▼                            ▼                            ▼
           ┌──────────────┐            ┌──────────────┐              ┌──────────────┐
           │  ws-gateway  │            │ audit-worker │              │ notification │
           │ (WebSocket)  │            │ (audit_db)   │              │ (tuỳ chọn)   │
           └──────────────┘            └──────────────┘              └──────────────┘
```

| Ai gọi ai / đọc gì | Giao thức |
|--------------------|-----------|
| **FE → ewallet-gateway** | REST |
| **ewallet-gateway → wallet-registry, transfer-service** | **gRPC** |
| **transfer-service → wallet-registry** (saga debit/credit) | **gRPC** |
| **transfer-service → Kafka** | Producer (command, outbox) |
| **ws-gateway, audit-worker, … → Kafka** | Consumer |
| **ws-gateway → FE** | **WebSocket** (push kết quả; user có thể refresh **GET số dư**) |

**Ghi chú:** `ws-gateway` **không** gọi gRPC tới wallet để lấy số dư; nó **consume** message kết quả (ví dụ `transfer.result`) và **push**. Số dư đọc qua **REST** (`GET /wallets/...`) như thiết kế hiện tại.

---

## 4. Tính năng / trách nhiệm từng service (để triển khai)

### 4.1. `ewallet-gateway`

| Hạng mục | Nội dung |
|----------|----------|
| **Vai trò** | Cổng **duy nhất** phía public: **REST**. FE **không** gọi thẳng microservice nội bộ. |
| **DB** | Không. |
| **Làm gì** | Route: auth, ví, chuyển tiền → **gRPC** tới `wallet-registry` / `transfer-service` (map DTO ↔ protobuf). CORS, rate limit; (sau) bảo vệ JWT ở edge nếu cần. |
| **Không làm** | Logic saga, không giữ WebSocket (thuộc `ws-gateway`). |

---

### 4.2. `wallet-registry`

| Hạng mục | Nội dung |
|----------|----------|
| **Vai trò** | **Nguồn sự thật** user + ví + **balance**. |
| **DB** | **`wallet_db`**. |
| **Làm gì** | Đăng ký / đăng nhập / refresh / logout; **một user ↔ một ví** (tạo ví khi đăng ký). **GET ví** (JWT, chủ ví) qua gateway. **gRPC** `Debit` / `Credit` (metadata `idempotency-key`, `x-internal-api-key` khi bật): transaction ngắn, `SELECT … FOR UPDATE`, idempotency table. |
| **Không làm** | Không publish Kafka; không gọi `transfer-service` trong transaction. |

---

### 4.3. `transfer-service`

| Hạng mục | Nội dung |
|----------|----------|
| **Vai trò** | Điều phối **chuyển tiền**: nhận lệnh, **ghi `transfer_db`**, **Kafka** command, **consumer** chạy saga **gRPC** tới wallet, **outbox** event. |
| **DB** | **`transfer_db`**. |
| **Làm gì** | `POST /transfers` (qua gateway): validate, idempotency `requestId`, ghi **ACCEPTED**, **publish `transfer.command`** (partition key **`fromWalletId`**), trả **202**. Worker: consume → saga debit/credit (idempotency từng bước) → terminal state → **outbox** `wallet.transfer.completed`, **`transfer.result`**. |
| **Không làm** | Không trở thành nguồn số dư; balance chỉ qua `wallet-registry`. |

---

### 4.4. `ws-gateway`

| Hạng mục | Nội dung |
|----------|----------|
| **Vai trò** | **WebSocket** real-time: user biết **kết quả giao dịch** (và có thể kèm gợi ý refresh UI). |
| **DB** | Không (tuỳ chọn **Redis** khi scale nhiều instance). |
| **Làm gì** | Auth socket (JWT); map `userId` ↔ session; **consume Kafka `transfer.result`** → **push** `{ requestId, status, … }` tới client đã subscribe. |
| **Không làm** | Không cập nhật balance; không thay cho **GET ví** để lấy số dư chính xác sau cùng. |

---

### 4.5. `audit-worker` / `notification-worker`

| Service | DB | Việc làm |
|---------|-----|----------|
| **audit-worker** | `audit_db` | Consume `wallet.transfer.completed` — ghi **append-only**. |
| **notification-worker** | `notification_db` | Consume `wallet.transfer.completed` — lưu **thông báo in-app** (title, body, `transfer_status`, `read_flag`, idempotent `requestId:userId`); **email** qua interface (stub log, SMTP sau). |

**Không** sửa `balance`.

---

## 5. Luồng chuyển tiền (tóm tắt)

```text
FE  ──POST /transfers──► gateway ──gRPC──► transfer-service
              │                              ├─ transfer_db: ACCEPTED
              │                              └─ Kafka: transfer.command (key = fromWalletId)
              ◄── 202 + requestId ───────────┘
                                              ▼
                                    consumer: gRPC debit/credit → wallet-registry
                                              ├─ outbox: completed + transfer.result
                                              └─ ...
FE  ◄── WebSocket ─── ws-gateway ◄── Kafka: transfer.result
```

- User **chờ** trên UI sau `202`; **kết quả** tới qua **WS** (hoặc poll API nếu WS lỗi).

---

## 6. Kafka — topic gợi ý

| Topic | Việc | Producer | Consumer |
|-------|------|----------|----------|
| **`transfer.command`** | Hàng đợi lệnh sau **202** | transfer-service | worker saga |
| **`wallet.transfer.completed`** | Sau commit thành công (audit, notify) | transfer-service (outbox) | audit-worker, notification-worker |
| **`transfer.result`** | `userId`, `requestId`, `status` — cho **push UI** | transfer-service (outbox) | **ws-gateway** |

**Partition:** `transfer.command` dùng **`fromWalletId`** để **tuần tự** lệnh cùng ví nguồn. `transfer.result` có thể partition theo **`userId`** cho consumer `ws-gateway`.

**Nhắc lại:** Kafka **không** ghi balance; chỉ **sự kiện / hàng đợi**.

---

## 7. API phía client (qua gateway)

- **`POST /api/v1/transfers`** → **202** + `{ requestId, status: "ACCEPTED" }`.
- **`GET /api/v1/transactions/...`** — poll trạng thái (dự phòng).
- **`GET /api/v1/wallets/{id}`** — đọc số dư (sau khi có kết quả, user có thể gọi lại).

---

## 8. Cấu trúc repo (gợi ý)

```
ewallet-backend/
├── proto/                    # .proto dùng chung (gRPC)
├── ewallet-gateway/
├── ws-gateway/
├── wallet-registry/
├── transfer-service/
├── audit-worker/
├── notification-worker/
└── docker-compose.yml        # MySQL (nhiều DB), Kafka, (tuỳ chọn) Redis
```

---

## 9. Lộ trình triển khai (G0 → G6)

| Giai đoạn | Việc làm |
|-----------|----------|
| **G0** | Docker: MySQL (multi-DB), Kafka, mạng nội bộ. |
| **G1** | **wallet-registry**: gRPC debit/credit + idempotency + auth/GET ví. |
| **G2** | **transfer-service**: **202** + `transfer.command` + consumer saga **gRPC** wallet. |
| **G3** | **ewallet-gateway**: REST → **gRPC**. |
| **G4** | Outbox + `wallet.transfer.completed` + `transfer.result`. |
| **G5** | **ws-gateway**: WebSocket + consume `transfer.result`. |
| **G6** | **audit-worker**, **notification-worker**. |

**Nối gRPC giữa service:** chuẩn bị `proto/`, codegen, DNS nội bộ (`wallet-registry:50051`, …); map lỗi gRPC → HTTP cho FE; (tuỳ chọn) TLS nội bộ, tắt REST internal trên prod.

---

## 10. Kiểm thử & checklist

**Kiểm thử:** idempotency (`requestId` + từng bước saga); saga lỗi → `transfer.result` **FAILED**; gRPC + WS; tải trước/sau khi có Kafka.

**Checklist:**

- [ ] Chuyển tiền: **202**, không chờ hết saga trên HTTP.
- [ ] Lệnh qua **Kafka** trước khi worker chạm DB ví.
- [ ] **`transfer.result`** → **ws-gateway** push.
- [ ] **wallet-registry** là nguồn balance; Kafka không ghi balance.
- [ ] **gRPC** nội bộ; **Kafka** message/outbox; **WebSocket** real-time cho user.

---

## 11. Liên kết

- Tổng quan: [README.md](./README.md)
- Frontend: [README-FE.md](./README-FE.md)
