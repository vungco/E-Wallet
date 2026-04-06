package com.app.ewallet.service;

import com.app.ewallet.client.WalletLedgerGrpcClient;
import com.app.ewallet.config.properties.KafkaTopicsProperties;
import com.app.ewallet.kafka.dto.TransferCommandPayload;
import com.app.ewallet.kafka.dto.TransferResultPayload;
import com.app.ewallet.kafka.dto.WalletTransferCompletedPayload;
import com.app.ewallet.model.EventOutbox;
import com.app.ewallet.model.Transfer;
import com.app.ewallet.model.TransferStatus;
import com.app.ewallet.redis.TransferResultRealtimePublisher;
import com.app.ewallet.repository.EventOutboxRepository;
import com.app.ewallet.repository.TransferRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferCommandProcessor {

    private final TransferRepository transferRepository;
    private final EventOutboxRepository eventOutboxRepository;
    private final WalletLedgerGrpcClient walletLedgerGrpcClient;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsProperties kafkaTopicsProperties;
    private final TransferResultRealtimePublisher transferResultRealtimePublisher;

    @Transactional
    public void process(TransferCommandPayload payload) {
        Transfer transfer = transferRepository.findByRequestId(payload.requestId()).orElse(null);
        if (transfer == null) {
            log.warn("Transfer not found for requestId={}", payload.requestId());
            return;
        }
        if (transfer.getStatus() == TransferStatus.SUCCESS || transfer.getStatus() == TransferStatus.FAILED) {
            return;
        }

        int claimed = transferRepository.updateStatusIf(
                payload.requestId(),
                TransferStatus.ACCEPTED,
                TransferStatus.PROCESSING
        );
        if (claimed == 0) {
            return;
        }

        transfer = transferRepository.findByRequestId(payload.requestId()).orElseThrow();
        BigDecimal amount = new BigDecimal(payload.amount()).setScale(4, RoundingMode.HALF_UP);

        try {
            walletLedgerGrpcClient.debit(
                    payload.fromWalletId(),
                    amount,
                    payload.requestId() + "-debit"
            );
        } catch (StatusRuntimeException e) {
            fail(transfer, e.getStatus().getDescription() != null
                    ? e.getStatus().getDescription()
                    : e.getMessage());
            return;
        }

        try {
            walletLedgerGrpcClient.credit(
                    payload.toWalletId(),
                    amount,
                    payload.requestId() + "-credit"
            );
        } catch (StatusRuntimeException e) {
            try {
                walletLedgerGrpcClient.credit(
                        payload.fromWalletId(),
                        amount,
                        payload.requestId() + "-rollback"
                );
            } catch (StatusRuntimeException rollbackEx) {
                log.error("Rollback failed for requestId={}", payload.requestId(), rollbackEx);
            }
            fail(transfer, e.getStatus().getDescription() != null
                    ? e.getStatus().getDescription()
                    : e.getMessage());
            return;
        }

        transfer.setStatus(TransferStatus.SUCCESS);
        transfer.setErrorMessage(null);
        transferRepository.save(transfer);
        enqueueSuccessEvents(transfer);
    }

    private void fail(Transfer transfer, String message) {
        transfer.setStatus(TransferStatus.FAILED);
        transfer.setErrorMessage(message != null && message.length() > 500 ? message.substring(0, 500) : message);
        transferRepository.save(transfer);
        enqueueFailureResults(transfer);
    }

    private void enqueueSuccessEvents(Transfer transfer) {
        try {
            String occurredAt = OffsetDateTime.now().toString();
            WalletTransferCompletedPayload completed = new WalletTransferCompletedPayload(
                    transfer.getId(),
                    transfer.getRequestId(),
                    transfer.getAmount(),
                    transfer.getFromUserId(),
                    transfer.getToUserId(),
                    occurredAt
            );
            EventOutbox completedRow = outboxRow(
                    transfer,
                    kafkaTopicsProperties.walletTransferCompleted(),
                    String.valueOf(transfer.getFromUserId()),
                    objectMapper.writeValueAsString(completed)
            );
            eventOutboxRepository.save(completedRow);

            String fromJson = objectMapper.writeValueAsString(new TransferResultPayload(
                    transfer.getFromUserId(),
                    transfer.getRequestId(),
                    "SUCCESS",
                    transfer.getId(),
                    null
            ));
            String toJson = objectMapper.writeValueAsString(new TransferResultPayload(
                    transfer.getToUserId(),
                    transfer.getRequestId(),
                    "SUCCESS",
                    transfer.getId(),
                    null
            ));
            scheduleTransferResultAfterCommit(List.of(fromJson, toJson));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private void enqueueFailureResults(Transfer transfer) {
        try {
            String err = transfer.getErrorMessage();
            String fromJson = objectMapper.writeValueAsString(new TransferResultPayload(
                    transfer.getFromUserId(),
                    transfer.getRequestId(),
                    "FAILED",
                    transfer.getId(),
                    err
            ));
            String toJson = objectMapper.writeValueAsString(new TransferResultPayload(
                    transfer.getToUserId(),
                    transfer.getRequestId(),
                    "FAILED",
                    transfer.getId(),
                    err
            ));
            scheduleTransferResultAfterCommit(List.of(fromJson, toJson));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private void scheduleTransferResultAfterCommit(List<String> jsonPayloads) {
        if (jsonPayloads.isEmpty()) {
            return;
        }
        Runnable publish = () -> {
            for (String json : jsonPayloads) {
                transferResultRealtimePublisher.publish(json);
            }
        };
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

    private static EventOutbox outboxRow(Transfer transfer, String topic, String partitionKey, String json) {
        EventOutbox e = new EventOutbox();
        e.setTransfer(transfer);
        e.setTopic(topic);
        e.setPartitionKey(partitionKey);
        e.setPayloadJson(json);
        return e;
    }
}
