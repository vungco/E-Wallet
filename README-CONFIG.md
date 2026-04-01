# 🚀 Project Structure – Mini E-Wallet (Spring Boot)

## 🎯 Mục tiêu

Xây dựng cấu trúc project rõ ràng, dễ mở rộng, tách biệt các layer theo clean architecture:

* Controller → nhận request
* Service → xử lý business logic
* Repository → làm việc với DB
* DTO → dữ liệu trao đổi
* Exception → xử lý lỗi tập trung

---

## 📁 Cấu trúc thư mục

```text
com.app.ewallet
│
├── config
├── model
├── repository
├── controller
│   └── dto
├── service
│   ├── interface
│   └── impl
├── exception
└── util (optional)
```

---

## 🔧 1. config

Chứa các class cấu hình hệ thống:

* Security config (JWT filter, authentication)
* Kafka config
* Database config
* CORS config

Ví dụ:

* `SecurityConfig`
* `KafkaProducerConfig`
* `KafkaConsumerConfig`

---

## 🗄️ 2. model

Chứa entity mapping với database:

* Tương ứng với table

Ví dụ:

* `User`
* `Wallet`
* `Transaction`

---

## 🧩 3. repository

* Interface extends JPA Repository
* Dùng để query DB

Ví dụ:

* `UserRepository`
* `WalletRepository`
* `TransactionRepository`

---

## 🌐 4. controller

* Nhận request từ client
* Validate input
* Gọi service

---

### 📦 dto (bên trong controller)

Chứa object request/response

```text
controller
 └── dto
      ├── TransferRequest
      ├── TransferResponse
      └── WalletResponse
```

---

## ⚙️ 5. service

### 📁 interface

* Định nghĩa method
* Không chứa logic

Ví dụ:

```java
public interface IWalletService {
    void transfer(TransferRequest request);
}
```

---

### 📁 impl

* Chứa logic xử lý chính
* Gọi repository

Ví dụ:

```java
public class WalletServiceImpl implements WalletService {
    // xử lý business logic
}
```

---

## ❗ 6. exception

### 🔥 ApiException (core)

* Exception base cho toàn hệ thống
* Chứa:

  * message
  * errorCode

---

### 📌 Các exception khác

Ví dụ:

* `InsufficientBalanceException`
* `UserNotFoundException`
* `DuplicateRequestException`

---

### 🌐 Global Exception Handler

* Bắt toàn bộ exception
* Trả response chuẩn về client

---

## 🧰 7. util (optional)

* Helper class
* Common function

Ví dụ:

* `JwtUtil`
* `DateUtil`

---

## 🔄 Flow xử lý request

```text
Client
 → Controller
     → DTO
     → Service
         → Repository
             → Database
```

---

## 📌 Quy tắc quan trọng

* Controller không chứa business logic
* Service không trả Entity trực tiếp → dùng DTO
* Repository chỉ dùng cho DB
* Exception phải xử lý tập trung
* Không gọi chéo layer sai quy tắc

---

## 🎯 Mục tiêu cuối cùng

* Code clean, dễ đọc
* Dễ scale lên microservice
* Có thể tách từng module sau này

---
