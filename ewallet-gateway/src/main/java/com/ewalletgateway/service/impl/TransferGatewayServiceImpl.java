package com.ewalletgateway.service.impl;

import com.app.ewallet.grpc.transfer.v1.GetTransferStatusRequest;
import com.app.ewallet.grpc.transfer.v1.TransferServiceGrpc;
import com.ewalletgateway.api.dto.AcceptedTransferResponse;
import com.ewalletgateway.api.dto.CreateTransferRequest;
import com.ewalletgateway.api.dto.TransferStatusResponse;
import com.ewalletgateway.config.TransferGrpcProperties;
import com.ewalletgateway.grpc.GatewayGrpcMetadataSupport;
import com.ewalletgateway.service.interfaces.ITransferGatewayService;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TransferGatewayServiceImpl implements ITransferGatewayService {

    private final ManagedChannel transferGrpcChannel;
    private final TransferGrpcProperties transferGrpcProperties;
    private final GatewayGrpcMetadataSupport grpcMetadata;

    @Override
    public AcceptedTransferResponse createTransfer(String authorizationHeader, CreateTransferRequest body) {
        TransferServiceGrpc.TransferServiceBlockingStub stub = stubWithAuth(metadataWithAuth(authorizationHeader));
        var b = com.app.ewallet.grpc.transfer.v1.CreateTransferRequest.newBuilder()
                .setRequestId(body.requestId())
                .setFromWalletId(body.fromWalletId())
                .setToWalletId(body.toWalletId())
                .setToUserId(body.toUserId())
                .setAmount(body.amount().toPlainString());
        if (body.fromUserEmail() != null && !body.fromUserEmail().isBlank()) {
            b.setFromUserEmail(body.fromUserEmail().trim());
        }
        if (body.toUserEmail() != null && !body.toUserEmail().isBlank()) {
            b.setToUserEmail(body.toUserEmail().trim());
        }
        var resp = stub.createTransfer(b.build());
        return new AcceptedTransferResponse(resp.getRequestId(), resp.getStatus());
    }

    @Override
    public TransferStatusResponse getTransferStatus(String authorizationHeader, String requestId) {
        TransferServiceGrpc.TransferServiceBlockingStub stub = stubWithAuth(metadataWithAuth(authorizationHeader));
        var r = stub.getTransferStatus(
                GetTransferStatusRequest.newBuilder().setRequestId(requestId).build()
        );
        String err = r.getErrorMessage();
        return new TransferStatusResponse(
                r.getRequestId(),
                r.getStatus(),
                r.getFromWalletId(),
                r.getToWalletId(),
                r.getAmount(),
                err == null || err.isEmpty() ? null : err
        );
    }

    private Metadata metadataWithAuth(String authorizationHeader) {
        Metadata md = new Metadata();
        md.put(GatewayGrpcMetadataSupport.AUTHORIZATION, authorizationHeader);
        grpcMetadata.putInternalApiKey(md, transferGrpcProperties.internalApiKey(), "transfer");
        return md;
    }

    private TransferServiceGrpc.TransferServiceBlockingStub stubWithAuth(Metadata md) {
        var intercepted = ClientInterceptors.intercept(
                transferGrpcChannel,
                MetadataUtils.newAttachHeadersInterceptor(md)
        );
        return TransferServiceGrpc.newBlockingStub(intercepted)
                .withDeadlineAfter(30, TimeUnit.SECONDS);
    }
}
