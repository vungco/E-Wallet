package com.app.ewallet.notificationworker.service;

import com.app.ewallet.notificationworker.email.EmailNotificationSender;
import com.app.ewallet.notificationworker.kafka.dto.WalletTransferCompletedPayload;
import com.app.ewallet.notificationworker.model.Notification;
import com.app.ewallet.notificationworker.model.NotificationTransferStatus;
import com.app.ewallet.notificationworker.model.NotificationUserRole;
import com.app.ewallet.notificationworker.redis.TransferResultRealtimePublisher;
import com.app.ewallet.notificationworker.redis.dto.TransferResultPayload;
import com.app.ewallet.notificationworker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferCompletedNotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailNotificationSender emailNotificationSender;
    private final ObjectMapper objectMapper;
    private final TransferResultRealtimePublisher transferResultRealtimePublisher;

    @Transactional
    public void createNotificationsIfAbsent(WalletTransferCompletedPayload payload) {
        upsertForUser(payload, payload.fromUserId(), NotificationUserRole.SENDER);
        upsertForUser(payload, payload.toUserId(), NotificationUserRole.RECEIVER);
    }

    private void upsertForUser(
            WalletTransferCompletedPayload p,
            long userId,
            NotificationUserRole role
    ) {
        String idempotencyKey = p.requestId() + ":" + userId;
        if (notificationRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.debug("Skip duplicate notification idempotencyKey={}", idempotencyKey);
            return;
        }

        long counterpart = role == NotificationUserRole.SENDER ? p.toUserId() : p.fromUserId();
        String title = role == NotificationUserRole.SENDER
                ? "Chuyển tiền thành công"
                : "Bạn nhận được tiền";
        String senderBal = p.fromBalanceAfter() != null
                ? (" Số dư ví sau giao dịch: " + p.fromBalanceAfter().toPlainString() + ".")
                : "";
        String receiverBal = p.toBalanceAfter() != null
                ? (" Số dư ví hiện tại: " + p.toBalanceAfter().toPlainString() + ".")
                : "";
        String timePart = p.timestamp() != null ? (" Thời điểm: " + p.timestamp() + ".") : "";
        String body = role == NotificationUserRole.SENDER
                ? ("Bạn đã chuyển " + p.amount().toPlainString()
                + " đến người nhận (userId=" + p.toUserId() + ")."
                + senderBal
                + " Mã lệnh: " + p.requestId() + "."
                + timePart)
                : ("Bạn nhận " + p.amount().toPlainString()
                + " từ người gửi (userId=" + p.fromUserId() + ")."
                + receiverBal
                + " Mã lệnh: " + p.requestId() + "."
                + timePart);

        Notification n = new Notification();
        n.setUserId(userId);
        n.setRequestId(p.requestId());
        n.setIdempotencyKey(idempotencyKey);
        n.setTitle(title);
        n.setBody(body);
        n.setTransferStatus(NotificationTransferStatus.SUCCESS);
        n.setReadFlag(false);
        n.setTransactionId(p.transactionId());
        n.setAmount(p.amount());
        n.setCounterpartUserId(counterpart);
        n.setUserRole(role);
        String recipientFromPayload =
                role == NotificationUserRole.SENDER ? p.fromEmail() : p.toEmail();
        n.setRecipientEmail(StringUtils.hasText(recipientFromPayload) ? recipientFromPayload.trim() : null);
        n.setEmailSent(false);
        n.setCreatedAt(Instant.now());

        notificationRepository.save(n);
        emailNotificationSender.sendTransferNotification(n);
        publishTransferResultRedis(userId, p);
    }

    private void publishTransferResultRedis(long userId, WalletTransferCompletedPayload p) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(new TransferResultPayload(
                    userId,
                    p.requestId(),
                    "SUCCESS",
                    p.transactionId(),
                    null
            ));
        } catch (Exception e) {
            log.warn("Failed to serialize transfer result for Redis userId={} requestId={}: {}",
                    userId, p.requestId(), e.getMessage());
            return;
        }
        Runnable publish = () -> transferResultRealtimePublisher.publish(json);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }
    }
}
