package com.ewalletgateway.client;

import com.app.ewallet.grpc.registry.v1.LoginUserRequest;
import com.app.ewallet.grpc.registry.v1.RefreshTokenMsg;
import com.app.ewallet.grpc.registry.v1.RegisterUserRequest;
import com.app.ewallet.grpc.registry.v1.RegisterUserResponse;
import com.app.ewallet.grpc.registry.v1.TokenPayload;
import com.app.ewallet.grpc.registry.v1.UserFundRequest;
import com.app.ewallet.grpc.registry.v1.WalletOperationResult;
import com.app.ewallet.grpc.registry.v1.WalletRegistryPublicGrpc;
import com.app.ewallet.grpc.registry.v1.WalletSnapshot;
import com.ewalletgateway.api.dto.LoginRequest;
import com.ewalletgateway.api.dto.RefreshTokenRequest;
import com.ewalletgateway.api.dto.RegisterRequest;
import com.ewalletgateway.api.dto.RegisterResponse;
import com.ewalletgateway.api.dto.TokenResponse;
import com.ewalletgateway.api.dto.WalletFundRequest;
import com.ewalletgateway.api.dto.WalletOperationResponse;
import com.ewalletgateway.api.dto.WalletResponse;
import com.ewalletgateway.config.properties.WalletRegistryProperties;
import com.google.protobuf.Empty;
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
public class WalletRegistryPublicGrpcClient {

    private static final Metadata.Key<String> KEY_INTERNAL =
            Metadata.Key.of("x-internal-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> KEY_AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final ManagedChannel walletRegistryGrpcChannel;
    private final WalletRegistryProperties walletRegistryProperties;

    public RegisterResponse register(RegisterRequest request) {
        RegisterUserResponse r = blockingStub(baseMetadata()).register(
                RegisterUserRequest.newBuilder()
                        .setName(request.name())
                        .setEmail(request.email())
                        .setPassword(request.password())
                        .build()
        );
        return new RegisterResponse(r.getUserId(), r.getEmail(), r.getName(), r.getMessage());
    }

    public TokenResponse login(LoginRequest request) {
        TokenPayload r = blockingStub(baseMetadata()).login(
                LoginUserRequest.newBuilder()
                        .setEmail(request.email())
                        .setPassword(request.password())
                        .build()
        );
        return toTokenResponse(r);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        TokenPayload r = blockingStub(baseMetadata()).refresh(
                RefreshTokenMsg.newBuilder()
                        .setRefreshToken(request.refreshToken())
                        .build()
        );
        return toTokenResponse(r);
    }

    public void logout(RefreshTokenRequest request) {
        blockingStub(baseMetadata()).logout(
                RefreshTokenMsg.newBuilder()
                        .setRefreshToken(request.refreshToken())
                        .build()
        );
    }

    public void logoutAll(String authorizationHeader) {
        Metadata md = baseMetadata();
        md.put(KEY_AUTHORIZATION, authorizationHeader);
        blockingStub(md).logoutAll(Empty.getDefaultInstance());
    }

    public WalletResponse getWallet(String authorizationHeader) {
        Metadata md = baseMetadata();
        md.put(KEY_AUTHORIZATION, authorizationHeader);
        WalletSnapshot w = blockingStub(md).getWallet(Empty.getDefaultInstance());
        return new WalletResponse(
                w.getWalletId(),
                w.getUserId(),
                w.getUserName(),
                new BigDecimal(w.getBalance()),
                w.getVersion()
        );
    }

    public WalletOperationResponse deposit(String authorizationHeader, WalletFundRequest body) {
        return mapFundResult(blockingStub(fundMetadata(authorizationHeader)).deposit(
                UserFundRequest.newBuilder()
                        .setAmount(body.amount().toPlainString())
                        .setIdempotencyKey(body.idempotencyKey().trim())
                        .build()
        ));
    }

    public WalletOperationResponse withdraw(String authorizationHeader, WalletFundRequest body) {
        return mapFundResult(blockingStub(fundMetadata(authorizationHeader)).withdraw(
                UserFundRequest.newBuilder()
                        .setAmount(body.amount().toPlainString())
                        .setIdempotencyKey(body.idempotencyKey().trim())
                        .build()
        ));
    }

    private Metadata fundMetadata(String authorizationHeader) {
        Metadata md = baseMetadata();
        md.put(KEY_AUTHORIZATION, authorizationHeader);
        return md;
    }

    private static WalletOperationResponse mapFundResult(WalletOperationResult r) {
        return new WalletOperationResponse(
                r.getWalletId(),
                new BigDecimal(r.getBalanceAfter()),
                r.getVersion(),
                r.getReplayed()
        );
    }

    private WalletRegistryPublicGrpc.WalletRegistryPublicBlockingStub blockingStub(Metadata headers) {
        return WalletRegistryPublicGrpc.newBlockingStub(walletRegistryGrpcChannel)
                .withDeadlineAfter(30, TimeUnit.SECONDS)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    private Metadata baseMetadata() {
        Metadata md = new Metadata();
        if (StringUtils.hasText(walletRegistryProperties.internalApiKey())) {
            md.put(KEY_INTERNAL, walletRegistryProperties.internalApiKey());
        }
        return md;
    }

    private static TokenResponse toTokenResponse(TokenPayload r) {
        return new TokenResponse(
                r.getAccessToken(),
                r.getRefreshToken(),
                r.getTokenType(),
                r.getExpiresIn()
        );
    }
}
