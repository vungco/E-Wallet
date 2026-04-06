package com.app.ewallet.service.impl;

import com.app.ewallet.client.WalletRegistryPublicGrpcClient;
import com.app.ewallet.client.dto.WalletRegistryWalletDto;
import com.app.ewallet.config.properties.KafkaTopicsProperties;
import com.app.ewallet.api.dto.AcceptedTransferResponse;
import com.app.ewallet.api.dto.CreateTransferRequest;
import com.app.ewallet.api.dto.TransferStatusResponse;
import com.app.ewallet.exception.DuplicateRequestException;
import com.app.ewallet.exception.InvalidTransferException;
import com.app.ewallet.exception.TransferAccessDeniedException;
import com.app.ewallet.exception.TransferNotFoundException;
import com.app.ewallet.kafka.dto.TransferCommandPayload;
import com.app.ewallet.model.Transfer;
import com.app.ewallet.model.TransferStatus;
import com.app.ewallet.repository.TransferRepository;
import com.app.ewallet.service.interfaces.ITransferService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements ITransferService {

    private final TransferRepository transferRepository;
    private final WalletRegistryPublicGrpcClient walletRegistryPublicGrpcClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Override
    @Transactional
    public AcceptedTransferResponse accept(CreateTransferRequest request, String accessToken, Long authenticatedUserId) {
        if (transferRepository.findByRequestId(request.requestId()).isPresent()) {
            throw new DuplicateRequestException(request.requestId());
        }
        if (request.fromWalletId().equals(request.toWalletId())) {
            throw new InvalidTransferException("fromWalletId and toWalletId must differ");
        }

        WalletRegistryWalletDto myWallet = walletRegistryPublicGrpcClient.getMyWallet(accessToken);
        if (myWallet == null
                || !myWallet.walletId().equals(request.fromWalletId())
                || !myWallet.userId().equals(authenticatedUserId)) {
            throw new InvalidTransferException("fromWalletId does not belong to the authenticated user");
        }

        var amount = request.amount().setScale(4, RoundingMode.HALF_UP);

        Transfer transfer = new Transfer();
        transfer.setRequestId(request.requestId());
        transfer.setFromWalletId(request.fromWalletId());
        transfer.setToWalletId(request.toWalletId());
        transfer.setFromUserId(authenticatedUserId);
        transfer.setToUserId(request.toUserId());
        transfer.setAmount(amount);
        transfer.setStatus(TransferStatus.ACCEPTED);
        transferRepository.save(transfer);

        TransferCommandPayload payload = new TransferCommandPayload(
                request.requestId(),
                request.fromWalletId(),
                request.toWalletId(),
                authenticatedUserId,
                request.toUserId(),
                amount.toPlainString()
        );

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize transfer command", e);
        }

        String topic = kafkaTopicsProperties.transferCommand();
        String partitionKey = String.valueOf(request.fromWalletId());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(topic, partitionKey, json);
            }
        });

        return new AcceptedTransferResponse(request.requestId(), "ACCEPTED");
    }

    @Override
    @Transactional(readOnly = true)
    public TransferStatusResponse getTransfer(String requestId, Long authenticatedUserId) {
        Transfer t = transferRepository.findByRequestId(requestId)
                .orElseThrow(() -> new TransferNotFoundException(requestId));
        if (!t.getFromUserId().equals(authenticatedUserId) && !t.getToUserId().equals(authenticatedUserId)) {
            throw new TransferAccessDeniedException();
        }
        return new TransferStatusResponse(
                t.getRequestId(),
                t.getStatus().name(),
                t.getFromWalletId(),
                t.getToWalletId(),
                t.getAmount(),
                t.getErrorMessage()
        );
    }
}
