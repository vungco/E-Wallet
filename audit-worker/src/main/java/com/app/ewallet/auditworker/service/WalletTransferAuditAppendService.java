package com.app.ewallet.auditworker.service;

import com.app.ewallet.auditworker.kafka.dto.WalletTransferCompletedPayload;
import com.app.ewallet.auditworker.model.WalletTransferCompletedAudit;
import com.app.ewallet.auditworker.repository.WalletTransferCompletedAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletTransferAuditAppendService {

    private final WalletTransferCompletedAuditRepository repository;

    /**
     * Ghi append-only; idempotent theo {@code transactionId} (replay Kafka / duplicate consumer).
     */
    @Transactional
    public void appendIfAbsent(WalletTransferCompletedPayload payload, String rawJson) {
        if (repository.existsByTransactionId(payload.transactionId())) {
            log.debug("Audit skip duplicate transactionId={}", payload.transactionId());
            return;
        }
        WalletTransferCompletedAudit row = new WalletTransferCompletedAudit();
        row.setTransactionId(payload.transactionId());
        row.setRequestId(payload.requestId());
        row.setAmount(payload.amount());
        row.setFromUserId(payload.fromUserId());
        row.setToUserId(payload.toUserId());
        row.setOccurredAt(payload.timestamp() != null ? payload.timestamp() : "");
        row.setPayloadJson(rawJson);
        row.setCreatedAt(Instant.now());
        try {
            repository.save(row);
        } catch (DataIntegrityViolationException e) {
            log.debug("Audit race duplicate transactionId={}", payload.transactionId());
        }
    }
}
