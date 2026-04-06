package com.app.ewallet.grpc;

import com.app.ewallet.config.properties.SecurityProperties;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Gắn cho {@code WalletRegistryPublic}: kiểm tra {@code x-internal-api-key} (khi bật);
 * metadata vào {@link RegistryGrpcContext}; với {@code DebitWallet}/{@code CreditWallet} bắt buộc
 * {@code idempotency-key} và gắn {@link LedgerGrpcContext}.
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        prefix = "app.grpc.server",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class GatewayRegistryGrpcInterceptor implements ServerInterceptor {

    private final SecurityProperties securityProperties;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        if (securityProperties.requireInternalKey()) {
            String expected = securityProperties.internalApiKey();
            if (!StringUtils.hasText(expected)) {
                call.close(Status.UNAUTHENTICATED.withDescription("INTERNAL_API_KEY is not configured"), new Metadata());
                return new ServerCall.Listener<>() {
                };
            }
            String provided = headers.get(GrpcMetadataKeys.X_INTERNAL_API_KEY);
            if (!expected.equals(provided)) {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid or missing x-internal-api-key"), new Metadata());
                return new ServerCall.Listener<>() {
                };
            }
        }

        String fullMethod = call.getMethodDescriptor().getFullMethodName();
        Context ctx = Context.current().withValue(RegistryGrpcContext.HEADERS, headers);
        if (isInternalLedgerMethod(fullMethod)) {
            String idempotency = headers.get(GrpcMetadataKeys.IDEMPOTENCY_KEY);
            if (!StringUtils.hasText(idempotency)) {
                call.close(Status.INVALID_ARGUMENT.withDescription("idempotency-key metadata is required"), new Metadata());
                return new ServerCall.Listener<>() {
                };
            }
            ctx = ctx.withValue(LedgerGrpcContext.IDEMPOTENCY_KEY, idempotency.trim());
        }
        return Contexts.interceptCall(ctx, call, headers, next);
    }

    private static boolean isInternalLedgerMethod(String fullMethodName) {
        return fullMethodName.endsWith("/DebitWallet") || fullMethodName.endsWith("/CreditWallet");
    }
}
