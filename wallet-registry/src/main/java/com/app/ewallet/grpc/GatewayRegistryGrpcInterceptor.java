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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GatewayRegistryGrpcInterceptor implements ServerInterceptor {

    private final SecurityProperties securityProperties;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String fullMethod = call.getMethodDescriptor().getFullMethodName();
        if (securityProperties.requireInternalKey()) {
            String expected = securityProperties.internalApiKey();
            if (!StringUtils.hasText(expected)) {
                log.warn(
                        "[internal-api-key] reject: server INTERNAL_API_KEY empty but app.security.require-internal-key=true"
                );
                call.close(Status.UNAUTHENTICATED.withDescription("INTERNAL_API_KEY is not configured"), new Metadata());
                return new ServerCall.Listener<>() {
                };
            }
            String provided = headers.get(GrpcMetadataKeys.X_INTERNAL_API_KEY);
            // Hai RPC khác nhau có thể dùng cùng thread — log method + độ dài, không log nội dung key
            log.debug(
                    "[internal-api-key] method={} providedLen={} missing={}",
                    fullMethod,
                    provided != null ? provided.length() : -1,
                    provided == null
            );
            if (!expected.equals(provided)) {
                logKeyMismatch(fullMethod, expected, provided);
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid or missing x-internal-api-key"), new Metadata());
                return new ServerCall.Listener<>() {
                };
            }
        }

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

    /**
     * Không log nội dung key — chỉ độ dài / null để soi lệch env hoặc khoảng trắng.
     */
    private static void logKeyMismatch(String fullMethod, String expected, String provided) {
        int lenE = expected != null ? expected.length() : -1;
        int lenP = provided != null ? provided.length() : -1;
        boolean trimWouldMatch =
                expected != null && provided != null && expected.trim().equals(provided.trim());
        log.warn(
                "[internal-api-key] reject method={} expectedLen={} providedLen={} providedHeaderMissing={} "
                        + "wouldMatchIfTrimmed={}. "
                        + "Nếu vừa thấy log khác có key rồi tới GetWallet thiếu key: client thứ 2 (thường transfer-service) chưa set INTERNAL_API_KEY.",
                fullMethod,
                lenE,
                lenP,
                provided == null,
                trimWouldMatch
        );
    }
}
