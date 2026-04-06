package com.app.ewallet.grpc;

import io.grpc.Context;

/**
 * Idempotency key được interceptor gắn vào Context cho RPC {@code DebitWallet}/{@code CreditWallet}
 * trên {@link com.app.ewallet.grpc.WalletRegistryPublicGrpcService}.
 */
public final class LedgerGrpcContext {

    public static final Context.Key<String> IDEMPOTENCY_KEY = Context.key("idempotency-key");

    private LedgerGrpcContext() {
    }
}
