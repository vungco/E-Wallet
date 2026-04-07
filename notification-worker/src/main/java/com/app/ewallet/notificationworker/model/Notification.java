package com.app.ewallet.notificationworker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Email nhận từ payload Kafka (fromEmail / toEmail) — SMTP chỉ dùng field này */
    @Column(name = "recipient_email", length = 320)
    private String recipientEmail;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    /** requestId + ":" + userId — idempotent consumer / retry Kafka */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false, length = 32)
    private NotificationTransferStatus transferStatus;

    @Column(name = "read_flag", nullable = false)
    private boolean readFlag;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "counterpart_user_id", nullable = false)
    private Long counterpartUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 16)
    private NotificationUserRole userRole;

    /** Bước sau: gửi SMTP — hiện stub chưa đánh dấu đã gửi */
    @Column(name = "email_sent", nullable = false)
    private boolean emailSent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
