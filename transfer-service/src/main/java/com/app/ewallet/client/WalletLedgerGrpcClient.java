package com.app.ewallet.client;

import com.app.ewallet.config.properties.WalletGrpcProperties;
import com.app.ewallet.grpc.wallet.v1.CreditWalletRequest;
import com.app.ewallet.grpc.wallet.v1.DebitWalletRequest;
import com.app.ewallet.grpc.wallet.v1.WalletLedgerGrpc;
import com.app.ewallet.grpc.wallet.v1.WalletOperationResponse;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WalletLedgerGrpcClient {

    private static final Metadata.Key<String> KEY_INTERNAL =
            Metadata.Key.of("x-internal-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> KEY_IDEMPOTENCY =
            Metadata.Key.of("idempotency-key", Metadata.ASCII_STRING_MARSHALLER);

    private final ManagedChannel walletRegistryGrpcChannel;
    private final WalletGrpcProperties walletGrpcProperties;

    public WalletOperationResponse debit(long walletId, BigDecimal amount, String idempotencyKey) {
        Metadata headers = buildMetadata(idempotencyKey);
        DebitWalletRequest req = DebitWalletRequest.newBuilder()
                .setWalletId(walletId)
                .setAmount(amount.toPlainString())
                .build();
        return blockingStub(headers).debitWallet(req);
    }

    public WalletOperationResponse credit(long walletId, BigDecimal amount, String idempotencyKey) {
        Metadata headers = buildMetadata(idempotencyKey);
        CreditWalletRequest req = CreditWalletRequest.newBuilder()
                .setWalletId(walletId)
                .setAmount(amount.toPlainString())
                .build();
        return blockingStub(headers).creditWallet(req);
    }

    private WalletLedgerGrpc.WalletLedgerBlockingStub blockingStub(Metadata headers) {
        return WalletLedgerGrpc.newBlockingStub(walletRegistryGrpcChannel)
                .withDeadlineAfter(30, TimeUnit.SECONDS)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    private Metadata buildMetadata(String idempotencyKey) {
        Metadata md = new Metadata();
        if (StringUtils.hasText(walletGrpcProperties.internalApiKey())) {
            md.put(KEY_INTERNAL, walletGrpcProperties.internalApiKey());
        }
        md.put(KEY_IDEMPOTENCY, idempotencyKey);
        return md;
    }
}
