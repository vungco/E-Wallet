package com.app.ewallet.client;

import com.app.ewallet.client.dto.WalletRegistryWalletDto;
import com.app.ewallet.config.properties.WalletGrpcProperties;
import com.app.ewallet.grpc.registry.v1.WalletRegistryPublicGrpc;
import com.app.ewallet.grpc.registry.v1.WalletSnapshot;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletRegistryPublicGrpcClient {

    private static final Metadata.Key<String> KEY_INTERNAL =
            Metadata.Key.of("x-internal-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> KEY_AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final ManagedChannel walletRegistryGrpcChannel;
    private final WalletGrpcProperties walletGrpcProperties;
    private final Environment environment;

    /**
     * Key dùng cho metadata {@code x-internal-api-key} (trùng wallet-registry / gateway). Gọi trước {@link #getMyWallet(String, String)}.
     */
    public String requireInternalApiKey() {
        String internalKey = resolveInternalApiKey();
        if (!StringUtils.hasText(internalKey)) {
            throw new IllegalStateException(
                    "transfer-service → wallet-registry: missing internal API key (app.grpc.wallet.internal-api-key). "
                            + "Set WALLET_INTERNAL_API_KEY or INTERNAL_API_KEY to the same value as wallet-registry. "
                            + "Do not use an empty WALLET_INTERNAL_API_KEY= line in .env (it overrides the YAML default)."
            );
        }
        return internalKey.trim();
    }

    /**
     * Ví của user tương ứng JWT. Bắt buộc JWT + {@code x-internal-api-key} (tham số {@code internalApiKey} — dùng {@link #requireInternalApiKey()}).
     */
    public WalletRegistryWalletDto getMyWallet(String bearerAccessToken, String internalApiKey) {
        if (!StringUtils.hasText(internalApiKey)) {
            throw new IllegalArgumentException("internalApiKey (wallet registry x-internal-api-key) must not be blank");
        }
        Metadata md = new Metadata();
        md.put(KEY_INTERNAL, internalApiKey.trim());
        md.put(KEY_AUTHORIZATION, "Bearer " + bearerAccessToken);
        try {
            WalletSnapshot w = blockingStub(md).getWallet(Empty.getDefaultInstance());
            return new WalletRegistryWalletDto(
                    w.getWalletId(),
                    w.getUserId(),
                    w.getUserName(),
                    new BigDecimal(w.getBalance()),
                    w.getVersion()
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED
                    || e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    private WalletRegistryPublicGrpc.WalletRegistryPublicBlockingStub blockingStub(Metadata headers) {
        return WalletRegistryPublicGrpc.newBlockingStub(walletRegistryGrpcChannel)
                .withDeadlineAfter(30, TimeUnit.SECONDS)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    /** Giống ewallet-gateway: yaml rồi fallback {@code INTERNAL_API_KEY}. */
    private String resolveInternalApiKey() {
        String raw = walletGrpcProperties.internalApiKey();
        if (!StringUtils.hasText(raw)) {
            raw = environment.getProperty("INTERNAL_API_KEY");
        }
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
