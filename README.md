# Mini E-Wallet — Tổng quan dự án

Hệ thống ví điện tử: **đúng số dư** (DB + khóa + idempotency + saga); **chuyển tiền async** qua **Kafka** (HTTP **202**, không chờ lock dài); **WebSocket `ws-gateway`** đẩy kết quả về client (giảm chờ, tăng chịu tải).

Chiến lược triển khai và tiêu chí xác thực:

| Tài liệu | Nội dung |
|----------|----------|
| [**README-BE.md**](./README-BE.md) | Microservice, **Kafka**, **`ws-gateway`**, `ewallet-gateway`, wallet-registry, transfer-service, workers. |
| [**README-FE.md**](./README-FE.md) | Nuxt: REST qua gateway + **WebSocket** tới `ws-gateway`. |

**Gợi ý tên repo:** `ewallet-backend`, `ewallet-frontend`.

---

*Đọc README-BE và README-FE trước khi code để thống nhất luồng async + WS.*
