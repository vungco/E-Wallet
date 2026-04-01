# Mini E-Wallet — Tổng quan dự án

Hệ thống ví điện tử: **đúng số dư** nhờ **khóa + idempotency + Saga**; backend **đã chốt microservice** — **mỗi service một database riêng**, Kafka cho audit/notify sau commit (outbox).

Chiến lược triển khai và tiêu chí xác thực được tách thành **hai tài liệu**:

| Tài liệu | Nội dung |
|----------|----------|
| [**README-BE.md**](./README-BE.md) | Backend: **microservice** (gateway, wallet-registry, transfer-service, workers), **mỗi service một DB**, lộ trình G0–G5. |
| [**README-FE.md**](./README-FE.md) | Frontend Nuxt 3: gọi API qua **gateway**, cấu trúc, checklist xác thực. |

**Gợi ý tên repo:** `ewallet-backend` (BE), `ewallet-frontend` (FE).

---

*Đọc README-BE và README-FE trước khi bắt đầu code để thống nhất ranh giới service/DB và thứ tự triển khai.*
