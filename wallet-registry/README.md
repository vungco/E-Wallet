# wallet-registry

Microservice quản lý **user / ví** trên database **`wallet_db`**.

Cấu trúc package bám [README-CONFIG.md](../README-CONFIG.md) (`config`, `model`, `repository`, `controller` + `dto`, `service` + `impl`, `exception`). Interface service đặt tên **`I...`** (ví dụ `IWalletQueryService`) trong package **`com.app.ewallet.service`** — không dùng package tên `interface` (trùng từ khóa Java).

## Swagger UI (SpringDoc)

Sau khi chạy app: **http://localhost:8081/swagger-ui/index.html** (port theo `SERVER_PORT`).

OpenAPI JSON: **http://localhost:8081/v3/api-docs**

## Biến môi trường (database & bảo mật)

| Biến | Mặc định | Ý nghĩa |
|------|----------|---------|
| `DB_HOST` | `localhost` | Host MySQL |
| `DB_PORT` | `3306` | Cổng MySQL |
| `DB_NAME` | `wallet_db` | Tên database |
| `DB_USERNAME` | `root` | User đăng nhập MySQL |
| `DB_PASSWORD` | *(rỗng)* | Mật khẩu |
| `DB_USE_SSL` | `false` | SSL JDBC |
| `DB_POOL_MAX` / `DB_POOL_MIN` | `10` / `2` | Hikari pool |
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile: `dev` tắt bắt buộc internal key (xem dưới) |
| `INTERNAL_API_KEY` | *(rỗng)* | Giá trị header `X-Internal-Api-Key` cho `/internal/**` |
| `REQUIRE_INTERNAL_KEY` | `true` | `true`: mọi request `/internal/**` phải gửi đúng key (trừ profile `dev` override trong yaml) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,...` | Danh sách origin FE (Nuxt), cách nhau bởi dấu phẩy |
| `SERVER_PORT` | `8081` | Cổng HTTP |
| `JWT_SECRET` | *(chuỗi dài mặc định trong yaml)* | **Bắt buộc đổi trên prod** — tối thiểu **32 byte** (HS256) |
| `JWT_ISSUER` | `wallet-registry` | Claim `iss` của access token |
| `JWT_ACCESS_MINUTES` | `15` | TTL access token (phút) |
| `JWT_REFRESH_DAYS` | `30` | TTL refresh token (ngày) |

**Profile `dev`:** trong `application.yaml`, `require-internal-key: false` để phát triển local không cần header. **Production:** dùng profile `prod` và đặt `INTERNAL_API_KEY` + `REQUIRE_INTERNAL_KEY=true`.

### Auth & refresh token

- **Access token:** JWT Bearer, dùng cho `Authorization` khi gọi `/api/v1/wallets/**` và `/api/v1/auth/logout/all`.
- **Refresh token:** chuỗi ngẫu nhiên (Base64 URL-safe); DB chỉ lưu **SHA-256 hex** (`token_hash`), có **UNIQUE** để tra cứu O(1) khi refresh/logout.
- **Luân phiên:** `POST /api/v1/auth/refresh` tạo refresh mới, thu hồi bản cũ (`revoked_at`, `replaced_by_id`).
- Index chi tiết: xem comment trong `V2__auth_and_refresh_tokens.sql` (`uk_refresh_tokens_token_hash`, `idx_refresh_tokens_user_id`, `idx_refresh_tokens_expires_at`, `idx_refresh_tokens_user_active`).

## Chạy local (MySQL đã có sẵn `wallet_db`)

```bash
export DB_PASSWORD=yourpassword
./gradlew bootRun
```

Flyway sẽ tạo bảng lần đầu (xem `src/main/resources/db/migration`).
