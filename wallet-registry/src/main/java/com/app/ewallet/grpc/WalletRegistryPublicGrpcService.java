package com.app.ewallet.grpc;

import com.app.ewallet.controller.dto.LoginRequest;
import com.app.ewallet.controller.dto.RefreshTokenRequest;
import com.app.ewallet.controller.dto.RegisterRequest;
import com.app.ewallet.controller.dto.InternalWalletOperationResponse;
import com.app.ewallet.controller.dto.RegisterResponse;
import com.app.ewallet.controller.dto.TokenResponse;
import com.app.ewallet.controller.dto.UserLookupResponse;
import com.app.ewallet.controller.dto.WalletResponse;
import com.app.ewallet.grpc.registry.v1.CreditWalletRequest;
import com.app.ewallet.grpc.registry.v1.DebitWalletRequest;
import com.app.ewallet.grpc.registry.v1.LoginUserRequest;
import com.app.ewallet.grpc.registry.v1.LookupUserByEmailRequest;
import com.app.ewallet.grpc.registry.v1.LookupUserByEmailResponse;
import com.app.ewallet.grpc.registry.v1.RefreshTokenMsg;
import com.app.ewallet.grpc.registry.v1.RegisterUserRequest;
import com.app.ewallet.grpc.registry.v1.RegisterUserResponse;
import com.app.ewallet.grpc.registry.v1.TokenPayload;
import com.app.ewallet.grpc.registry.v1.UserFundRequest;
import com.app.ewallet.grpc.registry.v1.WalletOperationResult;
import com.app.ewallet.grpc.registry.v1.WalletRegistryPublicGrpc;
import com.app.ewallet.grpc.registry.v1.WalletSnapshot;
import com.app.ewallet.security.JwtService;
import com.app.ewallet.security.UserPrincipal;
import com.app.ewallet.service.interfaces.IAuthService;
import com.app.ewallet.service.interfaces.IWalletLedgerService;
import com.app.ewallet.service.interfaces.IWalletQueryService;
import com.google.protobuf.Empty;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * API gRPC wallet-registry: auth/user (gateway), nạp/rút có JWT, và sổ cái nội bộ Debit/Credit (saga).
 */
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        prefix = "app.grpc.server",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class WalletRegistryPublicGrpcService extends WalletRegistryPublicGrpc.WalletRegistryPublicImplBase {

    private static final int IDEMPOTENCY_KEY_MAX_LEN = 128;

    private final IAuthService authService;
    private final IWalletQueryService walletQueryService;
    private final IWalletLedgerService walletLedgerService;
    private final JwtService jwtService;

    @Override
    public void register(RegisterUserRequest request, StreamObserver<RegisterUserResponse> responseObserver) {
        try {
            requireNonBlank(request.getName(), "name");
            requireNonBlank(request.getEmail(), "email");
            String password = request.getPassword();
            requireNonBlank(password, "password");
            if (password.length() < 8 || password.length() > 128) {
                throw new IllegalArgumentException("password length must be between 8 and 128");
            }
            RegisterResponse r = authService.register(new RegisterRequest(
                    request.getName().trim(),
                    request.getEmail().trim(),
                    password
            ));
            responseObserver.onNext(RegisterUserResponse.newBuilder()
                    .setUserId(r.userId())
                    .setEmail(r.email())
                    .setName(r.name())
                    .setMessage(r.message())
                    .build());
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void login(LoginUserRequest request, StreamObserver<TokenPayload> responseObserver) {
        try {
            requireNonBlank(request.getEmail(), "email");
            requireNonBlank(request.getPassword(), "password");
            TokenResponse r = authService.login(new LoginRequest(
                    request.getEmail().trim(),
                    request.getPassword()
            ));
            responseObserver.onNext(toTokenPayload(r));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void refresh(RefreshTokenMsg request, StreamObserver<TokenPayload> responseObserver) {
        try {
            requireNonBlank(request.getRefreshToken(), "refresh_token");
            TokenResponse r = authService.refresh(new RefreshTokenRequest(request.getRefreshToken()));
            responseObserver.onNext(toTokenPayload(r));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void logout(RefreshTokenMsg request, StreamObserver<Empty> responseObserver) {
        try {
            requireNonBlank(request.getRefreshToken(), "refresh_token");
            authService.logout(new RefreshTokenRequest(request.getRefreshToken()));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void logoutAll(Empty request, StreamObserver<Empty> responseObserver) {
        try {
            UserPrincipal principal = requireBearerUser();
            authService.logoutAllSessions(principal.userId());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void getWallet(Empty request, StreamObserver<WalletSnapshot> responseObserver) {
        try {
            UserPrincipal principal = requireBearerUser();
            WalletResponse w = walletQueryService.getWalletByUserId(principal.userId());
            responseObserver.onNext(WalletSnapshot.newBuilder()
                    .setWalletId(w.walletId())
                    .setUserId(w.userId())
                    .setUserName(w.userName())
                    .setBalance(w.balance().toPlainString())
                    .setVersion(w.version())
                    .build());
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void lookupUserByEmail(
            LookupUserByEmailRequest request,
            StreamObserver<LookupUserByEmailResponse> responseObserver
    ) {
        try {
            UserPrincipal principal = requireBearerUser();
            requireNonBlank(request.getEmail(), "email");
            UserLookupResponse u = walletQueryService.lookupUserByEmail(request.getEmail().trim());
            if (principal.userId().equals(u.userId())) {
                throw new IllegalArgumentException("Cannot transfer to yourself");
            }
            responseObserver.onNext(LookupUserByEmailResponse.newBuilder()
                    .setUserId(u.userId())
                    .setWalletId(u.walletId())
                    .setEmail(u.email())
                    .setName(u.name())
                    .build());
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void deposit(UserFundRequest request, StreamObserver<WalletOperationResult> responseObserver) {
        try {
            UserPrincipal principal = requireBearerUser();
            WalletResponse wallet = walletQueryService.getWalletByUserId(principal.userId());
            FundAmount params = parseFundAmount(request);
            var r = walletLedgerService.credit(wallet.walletId(), params.idempotencyKey(), params.amount());
            responseObserver.onNext(toWalletOperationResult(r));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void withdraw(UserFundRequest request, StreamObserver<WalletOperationResult> responseObserver) {
        try {
            UserPrincipal principal = requireBearerUser();
            WalletResponse wallet = walletQueryService.getWalletByUserId(principal.userId());
            FundAmount params = parseFundAmount(request);
            var r = walletLedgerService.debit(wallet.walletId(), params.idempotencyKey(), params.amount());
            responseObserver.onNext(toWalletOperationResult(r));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void debitWallet(DebitWalletRequest request, StreamObserver<WalletOperationResult> responseObserver) {
        String idempotencyKey = LedgerGrpcContext.IDEMPOTENCY_KEY.get();
        if (idempotencyKey == null) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT.withDescription("idempotency-key context missing").asRuntimeException()
            );
            return;
        }
        try {
            validateIdempotencyKey(idempotencyKey);
            requireNonBlankWalletId(request.getWalletId());
            BigDecimal amount = parsePositiveAmount(request.getAmount(), "amount");
            InternalWalletOperationResponse r = walletLedgerService.debit(
                    request.getWalletId(),
                    idempotencyKey.trim(),
                    amount
            );
            responseObserver.onNext(toWalletOperationResult(r));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    @Override
    public void creditWallet(CreditWalletRequest request, StreamObserver<WalletOperationResult> responseObserver) {
        String idempotencyKey = LedgerGrpcContext.IDEMPOTENCY_KEY.get();
        if (idempotencyKey == null) {
            responseObserver.onError(
                    io.grpc.Status.INVALID_ARGUMENT.withDescription("idempotency-key context missing").asRuntimeException()
            );
            return;
        }
        try {
            validateIdempotencyKey(idempotencyKey);
            requireNonBlankWalletId(request.getWalletId());
            BigDecimal amount = parsePositiveAmount(request.getAmount(), "amount");
            InternalWalletOperationResponse r = walletLedgerService.credit(
                    request.getWalletId(),
                    idempotencyKey.trim(),
                    amount
            );
            responseObserver.onNext(toWalletOperationResult(r));
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(GrpcExceptionMapper.toStatus(e));
        }
    }

    private static WalletOperationResult toWalletOperationResult(InternalWalletOperationResponse r) {
        return WalletOperationResult.newBuilder()
                .setWalletId(r.walletId())
                .setBalanceAfter(r.balanceAfter().toPlainString())
                .setVersion(r.version())
                .setReplayed(r.replayed())
                .build();
    }

    private static FundAmount parseFundAmount(UserFundRequest request) {
        validateIdempotencyKey(request.getIdempotencyKey());
        String idem = request.getIdempotencyKey().trim();
        BigDecimal amount = parsePositiveAmount(request.getAmount(), "amount");
        return new FundAmount(idem, amount);
    }

    private record FundAmount(String idempotencyKey, BigDecimal amount) {
    }

    private static void validateIdempotencyKey(String idem) {
        if (!StringUtils.hasText(idem)) {
            throw new IllegalArgumentException("idempotency_key must not be blank");
        }
        if (idem.trim().length() > IDEMPOTENCY_KEY_MAX_LEN) {
            throw new IllegalArgumentException("idempotency_key must be at most " + IDEMPOTENCY_KEY_MAX_LEN + " characters");
        }
    }

    private static BigDecimal parsePositiveAmount(String raw, String field) {
        requireNonBlank(raw, field);
        BigDecimal amount;
        try {
            amount = new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        return amount;
    }

    private static void requireNonBlank(String s, String field) {
        if (!StringUtils.hasText(s)) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void requireNonBlankWalletId(long walletId) {
        if (walletId <= 0) {
            throw new IllegalArgumentException("wallet_id must be positive");
        }
    }

    private UserPrincipal requireBearerUser() {
        Metadata md = RegistryGrpcContext.HEADERS.get(Context.current());
        if (md == null) {
            log.warn("[requireBearerUser] RegistryGrpcContext.HEADERS missing — interceptor did not attach metadata to Context");
            throw new IllegalStateException("Missing gRPC metadata context");
        }
        String auth = md.get(GrpcMetadataKeys.AUTHORIZATION);
        if (auth == null) {
            log.warn("[requireBearerUser] metadata 'authorization' absent (keys may be missing; check client sends lowercase header name)");
            throw io.grpc.Status.UNAUTHENTICATED.withDescription("Missing or invalid Authorization metadata").asRuntimeException();
        }
        if (!auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            log.warn(
                    "[requireBearerUser] authorization header must start with 'Bearer ' (prefix check failed); value length={}",
                    auth.length()
            );
            throw io.grpc.Status.UNAUTHENTICATED.withDescription("Missing or invalid Authorization metadata").asRuntimeException();
        }
        String token = auth.substring(7).trim();

        if (token.isEmpty()) {
            log.warn("[requireBearerUser] Bearer prefix present but access token is empty after trim");
            throw io.grpc.Status.UNAUTHENTICATED.withDescription("Missing or invalid Authorization metadata").asRuntimeException();
        }
        try {
            var claims = jwtService.parseAndValidate(token);
            return new UserPrincipal(jwtService.getUserId(claims), jwtService.getEmail(claims));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[requireBearerUser] JWT parse/validate failed: {} — check JWT_SECRET/issuer/expiry vs token", e.getMessage(), e);
            throw io.grpc.Status.UNAUTHENTICATED.withDescription("Invalid or expired access token").asRuntimeException();
        }
    }

    private static TokenPayload toTokenPayload(TokenResponse r) {
        return TokenPayload.newBuilder()
                .setAccessToken(r.accessToken())
                .setRefreshToken(r.refreshToken())
                .setTokenType(r.tokenType())
                .setExpiresIn(r.expiresInSeconds())
                .build();
    }
}
