package com.app.ewallet.grpc;

import com.app.ewallet.controller.dto.InternalWalletOperationResponse;
import com.app.ewallet.grpc.wallet.v1.CreditWalletRequest;
import com.app.ewallet.grpc.wallet.v1.DebitWalletRequest;
import com.app.ewallet.grpc.wallet.v1.WalletLedgerGrpc;
import com.app.ewallet.grpc.wallet.v1.WalletOperationResponse;
import com.app.ewallet.service.interfaces.IWalletLedgerService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        prefix = "app.grpc.server",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class WalletLedgerGrpcService extends WalletLedgerGrpc.WalletLedgerImplBase {

    private final IWalletLedgerService walletLedgerService;

    @Override
    public void debitWallet(DebitWalletRequest request, StreamObserver<WalletOperationResponse> responseObserver) {
        String idempotencyKey = LedgerGrpcContext.IDEMPOTENCY_KEY.get();
        if (idempotencyKey == null) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT.withDescription("idempotency-key context missing").asRuntimeException()
            );
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(request.getAmount());
            InternalWalletOperationResponse r = walletLedgerService.debit(
                    request.getWalletId(),
                    idempotencyKey,
                    amount
            );
            responseObserver.onNext(toProto(r));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void creditWallet(CreditWalletRequest request, StreamObserver<WalletOperationResponse> responseObserver) {
        String idempotencyKey = LedgerGrpcContext.IDEMPOTENCY_KEY.get();
        if (idempotencyKey == null) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT.withDescription("idempotency-key context missing").asRuntimeException()
            );
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(request.getAmount());
            InternalWalletOperationResponse r = walletLedgerService.credit(
                    request.getWalletId(),
                    idempotencyKey,
                    amount
            );
            responseObserver.onNext(toProto(r));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    private static WalletOperationResponse toProto(InternalWalletOperationResponse r) {
        return WalletOperationResponse.newBuilder()
                .setWalletId(r.walletId())
                .setBalanceAfter(r.balanceAfter().toPlainString())
                .setVersion(r.version())
                .setReplayed(r.replayed())
                .build();
    }
}
