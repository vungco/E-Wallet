package com.app.ewallet.grpc;

import io.grpc.Context;

/**
 * Idempotency key được interceptor gắn vào Context trước khi vào {@link com.app.ewallet.grpc.WalletLedgerGrpcService}.
 */
public final class LedgerGrpcContext {

    public static final Context.Key<String> IDEMPOTENCY_KEY = Context.key("idempotency-key");

    private LedgerGrpcContext() {
    }
}
