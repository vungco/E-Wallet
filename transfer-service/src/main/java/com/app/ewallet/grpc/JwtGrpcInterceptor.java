package com.app.ewallet.grpc;

import com.app.ewallet.security.JwtService;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        prefix = "app.grpc.server",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class JwtGrpcInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final JwtService jwtService;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String auth = headers.get(AUTHORIZATION);
        if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid authorization metadata"), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }
        String token = auth.substring(7).trim();
        try {
            Claims claims = jwtService.parseAndValidate(token);
            Long userId = jwtService.getUserId(claims);
            Context ctx = Context.current()
                    .withValue(TransferGrpcContext.USER_ID, userId)
                    .withValue(TransferGrpcContext.ACCESS_TOKEN, token);
            return Contexts.interceptCall(ctx, call, headers, next);
        } catch (Exception e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid or expired token").augmentDescription(e.getMessage()), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }
    }
}
