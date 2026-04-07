package com.app.ewallet.grpc;

import com.app.ewallet.api.dto.AcceptedTransferResponse;
import com.app.ewallet.api.dto.CreateTransferRequest;
import com.app.ewallet.api.dto.TransferStatusResponse;
import com.app.ewallet.grpc.transfer.v1.CreateTransferResponse;
import com.app.ewallet.grpc.transfer.v1.GetTransferStatusResponse;
import com.app.ewallet.grpc.transfer.v1.TransferServiceGrpc;
import com.app.ewallet.service.interfaces.ITransferService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        prefix = "app.grpc.server",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class TransferGrpcEndpoint extends TransferServiceGrpc.TransferServiceImplBase {

    private final ITransferService transferService;

    @Override
    public void createTransfer(
            com.app.ewallet.grpc.transfer.v1.CreateTransferRequest request,
            StreamObserver<CreateTransferResponse> responseObserver
    ) {
        Long userId = TransferGrpcContext.USER_ID.get();
        if (userId == null) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL.withDescription("User context missing").asRuntimeException()
            );
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(request.getAmount().trim());
            if (amount.compareTo(new BigDecimal("0.0001")) < 0) {
                throw new IllegalArgumentException("amount must be at least 0.0001");
            }
            String fromEmail = request.hasFromUserEmail() ? request.getFromUserEmail().trim() : null;
            if (fromEmail != null && fromEmail.isEmpty()) {
                fromEmail = null;
            }
            String toEmail = request.hasToUserEmail() ? request.getToUserEmail().trim() : null;
            if (toEmail != null && toEmail.isEmpty()) {
                toEmail = null;
            }
            CreateTransferRequest body = new CreateTransferRequest(
                    request.getRequestId(),
                    request.getFromWalletId(),
                    request.getToWalletId(),
                    request.getToUserId(),
                    amount,
                    fromEmail,
                    toEmail
            );
            String accessToken = TransferGrpcContext.ACCESS_TOKEN.get();
            if (accessToken == null) {
                responseObserver.onError(
                        io.grpc.Status.INTERNAL.withDescription("Access token context missing").asRuntimeException()
                );
                return;
            }
            AcceptedTransferResponse accepted = transferService.accept(body, accessToken, userId);
            responseObserver.onNext(
                    CreateTransferResponse.newBuilder()
                            .setRequestId(accepted.requestId())
                            .setStatus(accepted.status())
                            .build()
            );
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(TransferGrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void getTransferStatus(
            com.app.ewallet.grpc.transfer.v1.GetTransferStatusRequest request,
            StreamObserver<GetTransferStatusResponse> responseObserver
    ) {
        Long userId = TransferGrpcContext.USER_ID.get();
        if (userId == null) {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL.withDescription("User context missing").asRuntimeException()
            );
            return;
        }
        try {
            TransferStatusResponse r = transferService.getTransfer(request.getRequestId(), userId);
            responseObserver.onNext(
                    GetTransferStatusResponse.newBuilder()
                            .setRequestId(r.requestId())
                            .setStatus(r.status())
                            .setFromWalletId(r.fromWalletId())
                            .setToWalletId(r.toWalletId())
                            .setAmount(r.amount().toPlainString())
                            .setErrorMessage(r.errorMessage() != null ? r.errorMessage() : "")
                            .build()
            );
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(TransferGrpcExceptionMapper.toStatus(e));
        }
    }
}
