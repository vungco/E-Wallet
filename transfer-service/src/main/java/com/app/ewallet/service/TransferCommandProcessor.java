package com.app.ewallet.service;

import com.app.ewallet.client.WalletLedgerGrpcClient;
import com.app.ewallet.config.properties.KafkaTopicsProperties;
import com.app.ewallet.grpc.registry.v1.WalletOperationResult;
import com.app.ewallet.kafka.dto.TransferCommandPayload;
import com.app.ewallet.kafka.dto.WalletTransferCompletedPayload;
import com.app.ewallet.kafka.dto.WalletTransferFailedPayload;
import com.app.ewallet.model.EventOutbox;
import com.app.ewallet.model.Transfer;
import com.app.ewallet.model.TransferStatus;
import com.app.ewallet.repository.EventOutboxRepository;
import com.app.ewallet.repository.TransferRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferCommandProcessor {

    private final TransferRepository transferRepository;
    private final EventOutboxRepository eventOutboxRepository;
    private final WalletLedgerGrpcClient walletLedgerGrpcClient;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsProperties kafkaTopicsProperties;

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

        WalletOperationResult debitResult;
        try {
            debitResult = walletLedgerGrpcClient.debit(
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

        WalletOperationResult creditResult;
        try {
            creditResult = walletLedgerGrpcClient.credit(
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
        enqueueSuccessEvents(transfer, debitResult, creditResult);
    }

    private void fail(Transfer transfer, String message) {
        transfer.setStatus(TransferStatus.FAILED);
        transfer.setErrorMessage(message != null && message.length() > 500 ? message.substring(0, 500) : message);
        transferRepository.save(transfer);
        enqueueFailureEvent(transfer);
    }

    private void enqueueSuccessEvents(Transfer transfer, WalletOperationResult debitResult, WalletOperationResult creditResult) {
        try {
            String timestamp = Instant.now().toString();
            BigDecimal fromBalanceAfter = new BigDecimal(debitResult.getBalanceAfter().trim());
            BigDecimal toBalanceAfter = new BigDecimal(creditResult.getBalanceAfter().trim());
            WalletTransferCompletedPayload completed = new WalletTransferCompletedPayload(
                    transfer.getId(),
                    transfer.getRequestId(),
                    transfer.getAmount(),
                    transfer.getFromUserId(),
                    transfer.getToUserId(),
                    timestamp,
                    transfer.getFromUserEmail(),
                    transfer.getToUserEmail(),
                    fromBalanceAfter,
                    toBalanceAfter
            );
            EventOutbox completedRow = outboxRow(
                    transfer,
                    kafkaTopicsProperties.walletTransferCompleted(),
                    String.valueOf(transfer.getFromUserId()),
                    objectMapper.writeValueAsString(completed)
            );
            eventOutboxRepository.save(completedRow);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private void enqueueFailureEvent(Transfer transfer) {
        try {
            WalletTransferFailedPayload failed = new WalletTransferFailedPayload(
                    transfer.getId(),
                    transfer.getRequestId(),
                    transfer.getFromUserId(),
                    transfer.getToUserId(),
                    transfer.getErrorMessage()
            );
            EventOutbox row = outboxRow(
                    transfer,
                    kafkaTopicsProperties.walletTransferFailed(),
                    String.valueOf(transfer.getFromUserId()),
                    objectMapper.writeValueAsString(failed)
            );
            eventOutboxRepository.save(row);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
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
