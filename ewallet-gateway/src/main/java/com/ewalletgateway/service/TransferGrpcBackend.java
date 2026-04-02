package com.ewalletgateway.service;

import com.app.ewallet.grpc.transfer.v1.GetTransferStatusRequest;
import com.app.ewallet.grpc.transfer.v1.TransferServiceGrpc;
import com.ewalletgateway.api.dto.AcceptedTransferResponse;
import com.ewalletgateway.api.dto.CreateTransferRequest;
import com.ewalletgateway.api.dto.TransferStatusResponse;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TransferGrpcBackend {

    private static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final ManagedChannel transferGrpcChannel;

    public AcceptedTransferResponse createTransfer(String authorizationHeader, CreateTransferRequest body) {
        Metadata md = new Metadata();
        md.put(AUTHORIZATION, authorizationHeader);
        TransferServiceGrpc.TransferServiceBlockingStub stub = stubWithAuth(md);
        var resp = stub.createTransfer(
                com.app.ewallet.grpc.transfer.v1.CreateTransferRequest.newBuilder()
                        .setRequestId(body.requestId())
                        .setFromWalletId(body.fromWalletId())
                        .setToWalletId(body.toWalletId())
                        .setToUserId(body.toUserId())
                        .setAmount(body.amount().toPlainString())
                        .build()
        );
        return new AcceptedTransferResponse(resp.getRequestId(), resp.getStatus());
    }

    public TransferStatusResponse getTransferStatus(String authorizationHeader, String requestId) {
        Metadata md = new Metadata();
        md.put(AUTHORIZATION, authorizationHeader);
        TransferServiceGrpc.TransferServiceBlockingStub stub = stubWithAuth(md);
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

    private TransferServiceGrpc.TransferServiceBlockingStub stubWithAuth(Metadata md) {
        var intercepted = ClientInterceptors.intercept(
                transferGrpcChannel,
                MetadataUtils.newAttachHeadersInterceptor(md)
        );
        return TransferServiceGrpc.newBlockingStub(intercepted)
                .withDeadlineAfter(30, TimeUnit.SECONDS);
    }
}
