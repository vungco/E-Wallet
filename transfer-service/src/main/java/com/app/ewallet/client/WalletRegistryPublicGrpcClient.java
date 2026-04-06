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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WalletRegistryPublicGrpcClient {

    private static final Metadata.Key<String> KEY_INTERNAL =
            Metadata.Key.of("x-internal-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> KEY_AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final ManagedChannel walletRegistryGrpcChannel;
    private final WalletGrpcProperties walletGrpcProperties;

    /**
     * Ví của user tương ứng JWT (mỗi user một ví).
     */
    public WalletRegistryWalletDto getMyWallet(String bearerAccessToken) {
        Metadata md = new Metadata();
        if (StringUtils.hasText(walletGrpcProperties.internalApiKey())) {
            md.put(KEY_INTERNAL, walletGrpcProperties.internalApiKey());
        }
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
}
