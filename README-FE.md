# Mini E-Wallet — Chiến lược Frontend (FE)

Stack chốt: **Nuxt 3** (Vue 3), gọi REST từ backend.  
Repo gợi ý: **`ewallet-frontend`**.

---

## 1. Mục tiêu

- Giao diện **đơn giản, rõ**: xem số dư / ví, thực hiện chuyển tiền, xem lịch sử.
- **Không** coi frontend là nguồn sự thật về tiền: mọi số liệu cuối cùng lấy từ API; `requestId` chỉ hỗ trợ idempotency khi retry.
- Chuẩn bị **một base URL** trỏ **`ewallet-gateway`** (một biến môi trường); đổi host/port khi deploy **không đổi code** app.

---

## 2. Cấu hình môi trường

| Biến | Ý nghĩa |
|------|---------|
| `NUXT_PUBLIC_API_BASE` | URL **`ewallet-gateway`**, ví dụ `http://localhost:8080` |

Mọi request `$fetch` / `useFetch` dùng `joinURL` hoặc prefix từ biến này.

---

## 3. Cấu trúc thư mục gợi ý (Nuxt 3)

```
ewallet-frontend/
├── nuxt.config.ts
├── app.vue
├── pages/
│   ├── index.vue              # Dashboard / tóm tắt
│   ├── transfer.vue           # Form chuyển tiền
│   └── history.vue            # Lịch sử giao dịch
├── components/                # (tuỳ chọn) card số dư, bảng lịch sử
├── composables/
│   └── useApi.ts              # wrap base URL + lỗi chung
└── types/                     # DTO khớp API
```

Không bắt buộc Pinia nếu state chỉ là dữ liệu từ server; có thể thêm sau khi có auth.

---

## 4. Hành vi quan trọng: `requestId`

- Mỗi lần user **bắt đầu** một lệnh chuyển tiền mới: sinh **`crypto.randomUUID()`** (hoặc tương đương) **một lần** cho payload.
- Nếu lỗi mạng và user **retry cùng một thao tác** (cùng form chưa reset): **giữ nguyên** `requestId` để backend từ chối trùng an toàn.
- Nếu user **bấm chuyển lần mới**: sinh `requestId` mới.

---

## 5. Mapping màn hình ↔ API (cập nhật khi BE chốt OpenAPI)

| Màn hình | Hành động | API (ví dụ path — qua gateway, khớp README-BE) |
|----------|-----------|--------------------------------------|
| Dashboard | Hiển thị user / ví / số dư | `GET` … `/users`, `/wallets/...` (theo BE) |
| Chuyển tiền | Gửi form | `POST /api/v1/transfers` + `requestId` |
| Lịch sử | Danh sách | `GET /api/v1/transactions` (query phân trang nếu có) |

Đường dẫn chính xác điền vào đây sau khi backend đóng contract.

---

## 6. Xử lý lỗi & UX tối thiểu

- Hiển thị message từ body lỗi BE (ví dụ không đủ tiền, validation).
- Loading state khi `POST` transfer; disable double-submit (nhưng vẫn dựa vào idempotency phía server).
- (Tuỳ chọn) Toast / alert thành công-thất bại.

---

## 7. Lộ trình thực thi FE

| Bước | Việc làm | Phụ thuộc |
|------|----------|-----------|
| **F1** | Khởi tạo Nuxt 3, layout, `NUXT_PUBLIC_API_BASE` → gateway, trang health | Gateway + BE đã có endpoint health |
| **F2** | Trang dashboard: gọi API lấy số dư / ví (qua gateway) | `wallet-registry` đã expose qua gateway |
| **F3** | Trang chuyển tiền: form + `requestId` + `POST` transfer | `POST /api/v1/transfers` qua gateway |
| **F4** | Trang lịch sử: `GET` danh sách | `transfer-service` (hoặc route gateway tương ứng) |
| **F5** | (Tuỳ chọn) Thêm header auth khi gateway bật bảo vệ | Cùng `NUXT_PUBLIC_API_BASE` |

---

## 8. Tiêu chí xác thực FE (checklist)

**Đối chiếu BE microservice (qua gateway)**

- [ ] Chạy `npm`/`pnpm`/`yarn` dev, không lỗi build.
- [ ] `NUXT_PUBLIC_API_BASE` trỏ đúng **`ewallet-gateway`** (cùng stack Compose với BE).
- [ ] Xem được số dư (API thật qua gateway).
- [ ] Chuyển tiền thành công; lịch sử có giao dịch mới.
- [ ] Gửi lại cùng `requestId` (mô phỏng retry) → không trừ hai lần (kiểm tra `transfer-service` + wallet).
- [ ] Thử số tiền không hợp lệ / không đủ tiền → UI hiển thị lỗi rõ.

---

## 9. Kiểm thử tự động (tuỳ chọn, sau khi xác thực thủ công)

- Playwright / Vitest + MSW: mock API cho CI.
- E2E: chạy FE + stack BE (gateway + microservices) trong Compose, kịch bản “chuyển tiền + xem lịch sử”.

---

## 10. Liên kết

- Tổng quan: [README.md](./README.md)  
- Backend (microservice): [README-BE.md](./README-BE.md)
